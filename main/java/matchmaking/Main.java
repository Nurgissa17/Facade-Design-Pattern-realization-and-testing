package matchmaking;
import matchmaking.facade.AdminFacade;
import matchmaking.facade.MatchmakingFacade;
import matchmaking.subsystem.a.PartyRepository;
import matchmaking.subsystem.a.SkillRatingService;
import matchmaking.subsystem.b.LatencyChecker;
import matchmaking.subsystem.b.ServerRepository;
import matchmaking.subsystem.c.AntiCheatScanner;
import matchmaking.subsystem.c.SessionLogger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Path dataDirectory = Path.of("data");
        dataDirectory.toFile().mkdirs();
        SkillRatingService skillRatingService = new SkillRatingService();
        PartyRepository partyRepository = new PartyRepository(dataDirectory);
        LatencyChecker latencyChecker = new LatencyChecker();
        ServerRepository serverRepository = new ServerRepository(dataDirectory);
        AntiCheatScanner antiCheatScanner = new AntiCheatScanner();
        SessionLogger sessionLogger = new SessionLogger(dataDirectory);
        ProgressStore progressStore = new ProgressStore(dataDirectory);
        MatchmakingFacade matchmakingFacade = new MatchmakingFacade(
                skillRatingService,
                partyRepository,
                latencyChecker,
                serverRepository,
                antiCheatScanner,
                sessionLogger,
                progressStore
        );
        AdminFacade adminFacade = new AdminFacade(
                matchmakingFacade,
                partyRepository,
                serverRepository,
                sessionLogger,
                progressStore
        );
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1 - Normal matchmaking");
            System.out.println("2 - Trigger B failure");
            System.out.println("3 - Trigger C failure");
            System.out.println("4 - Inspect state");
            System.out.println("5 - Manual compensation");
            System.out.println("0 - Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine();
            try {
                if (choice.equals("1")) {
                    runMatch(scanner, matchmakingFacade, false, false);
                } else if (choice.equals("2")) {
                    runMatch(scanner, matchmakingFacade, true, false);
                } else if (choice.equals("3")) {
                    runMatch(scanner, matchmakingFacade, false, true);
                } else if (choice.equals("4")) {
                    System.out.print("Match id: ");
                    System.out.println(adminFacade.inspect(scanner.nextLine()));
                } else if (choice.equals("5")) {
                    System.out.print("Match id: ");
                    adminFacade.compensate(scanner.nextLine());
                    System.out.println("Compensated.");
                } else if (choice.equals("0")) {
                    return;
                }
            } catch (RuntimeException e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }
    private static void runMatch(
            Scanner scanner,
            MatchmakingFacade facade,
            boolean failB,
            boolean failC) {
        System.out.print("Match id: ");
        String matchId = scanner.nextLine();
        System.out.println("Enter 10 ratings separated by commas:");
        String[] parts = scanner.nextLine().split(",");
        List<Integer> ratings = new ArrayList<>();
        for (String part : parts) {
            ratings.add(Integer.parseInt(part.trim()));
        }
        int latencyMs;
        if (failB) {
            latencyMs = 150;
        } else {
            System.out.print("Latency in ms: ");
            latencyMs = Integer.parseInt(scanner.nextLine());
        }
        facade.findMatch(matchId, ratings, latencyMs, failC);
        System.out.println("Match completed.");
    }
}