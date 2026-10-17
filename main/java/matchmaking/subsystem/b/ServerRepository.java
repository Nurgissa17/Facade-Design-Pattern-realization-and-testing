package matchmaking.subsystem.b;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
public class ServerRepository {
    private final Path file;
    public ServerRepository(Path dataDirectory) {
        file = dataDirectory.resolve("servers.csv");
    }
    public void save(ServerAllocator server) {
        String row = server.getMatchId() + "," + server.getLatencyMs() + "," + server.getStatus() + "\n";
        try {
            Files.writeString(file, row, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public ServerAllocator find(String matchId) {
        if (Files.notExists(file)) {
            return null;
        }
        ServerAllocator result = null;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.split(",");
                if (p[0].equals(matchId)) {
                    result = new ServerAllocator(p[0], Integer.parseInt(p[1]), ServerAllocator.Status.valueOf(p[2]));
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}