package matchmaking;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
public class ProgressStore {
    public enum Status { PENDING_B, PENDING_C, COMPLETED, COMPENSATED }
    private final Path file;
    public ProgressStore(Path dataDirectory) {
        file = dataDirectory.resolve("progress.csv");
    }
    public void save(String matchId, int latency, boolean flagged, Status status) {
        String row = matchId + "," + latency + "," + flagged + "," + status + "\n";
        try {
            Files.writeString(file, row, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String[] find(String matchId) {
        if (Files.notExists(file)) {
            return null;
        }
        String[] result = null;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.split(",");
                if (p[0].equals(matchId)) {
                    result = p;
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String[] load(String matchId) {
        String[] result = find(matchId);
        if (result == null) {
            throw new MatchmakingException("Match not found.");
        }
        return result;
    }
}
