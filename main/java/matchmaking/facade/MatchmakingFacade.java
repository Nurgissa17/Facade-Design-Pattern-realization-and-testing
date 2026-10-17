package matchmaking.facade;
import matchmaking.MatchmakingException;
import matchmaking.ProgressStore;
import matchmaking.subsystem.a.PartyFormation;
import matchmaking.subsystem.a.PartyRepository;
import matchmaking.subsystem.a.SkillRatingService;
import matchmaking.subsystem.b.LatencyChecker;
import matchmaking.subsystem.b.ServerAllocator;
import matchmaking.subsystem.b.ServerRepository;
import matchmaking.subsystem.c.AntiCheatScanner;
import matchmaking.subsystem.c.GameSession;
import matchmaking.subsystem.c.SessionLogger;
import java.util.List;
public class MatchmakingFacade {
    private final SkillRatingService skillRating;
    private final PartyRepository parties;
    private final LatencyChecker latency;
    private final ServerRepository servers;
    private final AntiCheatScanner antiCheat;
    private final SessionLogger sessions;
    private final ProgressStore progress;
    public MatchmakingFacade(
            SkillRatingService skillRating,
            PartyRepository parties,
            LatencyChecker latency,
            ServerRepository servers,
            AntiCheatScanner antiCheat,
            SessionLogger sessions,
            ProgressStore progress) {
        this.skillRating = skillRating;
        this.parties = parties;
        this.latency = latency;
        this.servers = servers;
        this.antiCheat = antiCheat;
        this.sessions = sessions;
        this.progress = progress;
    }
    public void findMatch(String id, List<Integer> ratings, int ping, boolean flagged) {
        PartyFormation party = new PartyFormation(id, skillRating.averageRating(ratings), PartyFormation.Status.NEW);
        party.form();
        parties.save(party);
        progress.save(id, ping, flagged, ProgressStore.Status.PENDING_B);
        resumeMatch(id);
    }
    public void resumeMatch(String id) {
        String[] p = progress.load(id);
        ProgressStore.Status status = ProgressStore.Status.valueOf(p[3]);
        int ping = Integer.parseInt(p[1]);
        boolean flagged = Boolean.parseBoolean(p[2]);
        if (status == ProgressStore.Status.PENDING_B) {
            if (!latency.isAcceptable(ping)) {
                compensate(id);
                throw new MatchmakingException("Latency is too high.");
            }
            ServerAllocator server = new ServerAllocator(id, ping, ServerAllocator.Status.NEW);
            server.allocate();
            servers.save(server);
            progress.save(id, ping, flagged, ProgressStore.Status.PENDING_C);
            status = ProgressStore.Status.PENDING_C;
        }
        if (status == ProgressStore.Status.PENDING_C) {
            GameSession session = new GameSession(id, GameSession.Status.NEW);
            if (!antiCheat.passes(flagged)) {
                session.reject();
                sessions.save(session);
                compensate(id);
                throw new MatchmakingException("Anti-cheat rejected the match.");
            }
            session.confirm();
            sessions.save(session);
            progress.save(id, ping, flagged, ProgressStore.Status.COMPLETED);
        }
    }
    public void compensate(String id) {
        String[] p = progress.load(id);
        ProgressStore.Status status = ProgressStore.Status.valueOf(p[3]);
        if (status == ProgressStore.Status.PENDING_C) {
            ServerAllocator server = servers.find(id);
            server.release();
            servers.save(server);
        }
        if (status == ProgressStore.Status.PENDING_B || status == ProgressStore.Status.PENDING_C) {
            PartyFormation party = parties.find(id);
            party.cancel();
            parties.save(party);
            progress.save(id, Integer.parseInt(p[1]), Boolean.parseBoolean(p[2]), ProgressStore.Status.COMPENSATED);
            return;
        }
        throw new MatchmakingException("Cannot compensate this match.");
    }
}
