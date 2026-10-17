package matchmaking;

import matchmaking.facade.AdminFacade;
import matchmaking.facade.MatchmakingFacade;
import matchmaking.subsystem.a.PartyFormation;
import matchmaking.subsystem.a.PartyRepository;
import matchmaking.subsystem.a.SkillRatingService;
import matchmaking.subsystem.b.LatencyChecker;
import matchmaking.subsystem.b.ServerAllocator;
import matchmaking.subsystem.b.ServerRepository;
import matchmaking.subsystem.c.AntiCheatScanner;
import matchmaking.subsystem.c.GameSession;
import matchmaking.subsystem.c.SessionLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MatchmakingTest {
    @TempDir
    Path dir;
    PartyRepository parties;
    ServerRepository servers;
    SessionLogger sessions;
    ProgressStore progress;
    MatchmakingFacade facade;
    AdminFacade admin;

    private void start() {
        SkillRatingService skill = new SkillRatingService();
        LatencyChecker latency = new LatencyChecker();
        AntiCheatScanner antiCheat = new AntiCheatScanner();
        parties = new PartyRepository(dir);
        servers = new ServerRepository(dir);
        sessions = new SessionLogger(dir);
        progress = new ProgressStore(dir);
        facade = new MatchmakingFacade(skill, parties, latency, servers, antiCheat, sessions, progress);
        admin = new AdminFacade(facade, parties, servers, sessions, progress);
    }

    private List<Integer> ratings() {
        return List.of(1000, 1020, 1040, 1060, 1080, 1100, 1120, 1140, 1160, 1180);
    }

    private void thenProgressIs(String id, ProgressStore.Status status) {
        assertEquals(status.name(), progress.find(id)[3]);
    }

    @Test
    void success() {
        start();
        facade.findMatch("1", ratings(), 50, false);
        assertEquals(PartyFormation.Status.FORMED, parties.find("1").getStatus());
        assertEquals(ServerAllocator.Status.ALLOCATED, servers.find("1").getStatus());
        assertEquals(GameSession.Status.CONFIRMED, sessions.find("1").getStatus());
        thenProgressIs("1", ProgressStore.Status.COMPLETED);
    }

    @Test
    void bFailure() {
        start();
        assertThrows(MatchmakingException.class, () -> facade.findMatch("2", ratings(), 150, false));
        assertEquals(PartyFormation.Status.CANCELLED, parties.find("2").getStatus());
        assertNull(servers.find("2"));
        assertNull(sessions.find("2"));
        thenProgressIs("2", ProgressStore.Status.COMPENSATED);
    }

    @Test
    void cFailure() {
        start();
        assertThrows(MatchmakingException.class, () -> facade.findMatch("3", ratings(), 50, true));
        assertEquals(PartyFormation.Status.CANCELLED, parties.find("3").getStatus());
        assertEquals(ServerAllocator.Status.RELEASED, servers.find("3").getStatus());
        assertEquals(GameSession.Status.REJECTED, sessions.find("3").getStatus());
        thenProgressIs("3", ProgressStore.Status.COMPENSATED);
    }

    @Test
    void invalidInput() {
        start();
        assertThrows(MatchmakingException.class, () -> facade.findMatch("4", List.of(1000), 50, false));
        assertNull(parties.find("4"));
        assertNull(progress.find("4"));
    }

    @Test
    void restart() {
        start();
        PartyFormation party = new PartyFormation("5", 1100, PartyFormation.Status.NEW);
        party.form();
        parties.save(party);
        progress.save("5", 50, false, ProgressStore.Status.PENDING_B);
        start();
        facade.resumeMatch("5");
        thenProgressIs("5", ProgressStore.Status.COMPLETED);
    }

    @Test
    void adminFacade() {
        start();
        PartyFormation party = new PartyFormation("6", 1100, PartyFormation.Status.NEW);
        party.form();
        parties.save(party);
        ServerAllocator server = new ServerAllocator("6", 50, ServerAllocator.Status.NEW);
        server.allocate();
        servers.save(server);
        progress.save("6", 50, false, ProgressStore.Status.PENDING_C);
        assertEquals("Party=FORMED, Server=ALLOCATED, Session=NONE, Progress=PENDING_C", admin.inspect("6"));
        admin.compensate("6");
        thenProgressIs("6", ProgressStore.Status.COMPENSATED);
    }

    @Test
    void subsystemsWork() {
        SkillRatingService skill = new SkillRatingService();
        PartyRepository partyRepository = new PartyRepository(dir);
        PartyFormation party = new PartyFormation("a", skill.averageRating(ratings()), PartyFormation.Status.NEW);
        assertThrows(IllegalStateException.class, party::cancel);
        party.form();
        partyRepository.save(party);
        assertEquals(PartyFormation.Status.FORMED, partyRepository.find("a").getStatus());

        LatencyChecker latency = new LatencyChecker();
        ServerRepository serverRepository = new ServerRepository(dir);
        assertTrue(latency.isAcceptable(50));
        ServerAllocator server = new ServerAllocator("b", 50, ServerAllocator.Status.NEW);
        assertThrows(IllegalStateException.class, server::release);
        server.allocate();
        serverRepository.save(server);
        assertEquals(ServerAllocator.Status.ALLOCATED, serverRepository.find("b").getStatus());

        AntiCheatScanner antiCheat = new AntiCheatScanner();
        SessionLogger sessionLogger = new SessionLogger(dir);
        assertTrue(antiCheat.passes(false));
        GameSession session = new GameSession("c", GameSession.Status.NEW);
        session.confirm();
        assertThrows(IllegalStateException.class, session::reject);
        sessionLogger.save(session);
        assertEquals(GameSession.Status.CONFIRMED, sessionLogger.find("c").getStatus());
    }
}
