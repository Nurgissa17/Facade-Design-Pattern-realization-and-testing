package matchmaking.facade;
import matchmaking.ProgressStore;
import matchmaking.subsystem.a.PartyFormation;
import matchmaking.subsystem.a.PartyRepository;
import matchmaking.subsystem.b.ServerAllocator;
import matchmaking.subsystem.b.ServerRepository;
import matchmaking.subsystem.c.GameSession;
import matchmaking.subsystem.c.SessionLogger;
public class AdminFacade {
    private final MatchmakingFacade matchmakingFacade;
    private final PartyRepository partyRepository;
    private final ServerRepository serverRepository;
    private final SessionLogger sessionLogger;
    private final ProgressStore progressStore;
    public AdminFacade(
            MatchmakingFacade matchmakingFacade,
            PartyRepository partyRepository,
            ServerRepository serverRepository,
            SessionLogger sessionLogger,
            ProgressStore progressStore) {
        this.matchmakingFacade = matchmakingFacade;
        this.partyRepository = partyRepository;
        this.serverRepository = serverRepository;
        this.sessionLogger = sessionLogger;
        this.progressStore = progressStore;
    }
    public String inspect(String matchId) {
        PartyFormation party = partyRepository.find(matchId);
        ServerAllocator server = serverRepository.find(matchId);
        GameSession session = sessionLogger.find(matchId);
        String[] progress = progressStore.find(matchId);
        String partyStatus = party == null ? "NONE" : party.getStatus().name();
        String serverStatus = server == null ? "NONE" : server.getStatus().name();
        String sessionStatus = session == null ? "NONE" : session.getStatus().name();
        String progressStatus = progress == null ? "NONE" : progress[3];
        return "Party=" + partyStatus +
                ", Server=" + serverStatus +
                ", Session=" + sessionStatus +
                ", Progress=" + progressStatus;
    }
    public void compensate(String matchId) {
        matchmakingFacade.compensate(matchId);
    }
}
