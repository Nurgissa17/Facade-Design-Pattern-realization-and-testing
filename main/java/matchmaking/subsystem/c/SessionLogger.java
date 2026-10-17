package matchmaking.subsystem.c;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
public class SessionLogger {
    private final Path file;
    public SessionLogger(Path dataDirectory) {
        file = dataDirectory.resolve("sessions.csv");
    }
    public void save(GameSession session) {
        String row = session.getMatchId() + "," + session.getStatus() + "\n";
        try {
            Files.writeString(file, row, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public GameSession find(String matchId) {
        if (Files.notExists(file)) {
            return null;
        }
        GameSession result = null;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.split(",");
                if (p[0].equals(matchId)) {
                    result = new GameSession(p[0], GameSession.Status.valueOf(p[1]));
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
