package matchmaking.subsystem.a;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
public class PartyRepository {
    private final Path file;
    public PartyRepository(Path dataDirectory) {
        file = dataDirectory.resolve("parties.csv");
    }
    public void save(PartyFormation party) {
        String row = party.getMatchId() + "," + party.getAverageRating() + "," + party.getStatus() + "\n";
        try {
            Files.writeString(file, row, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public PartyFormation find(String matchId) {
        if (Files.notExists(file)) {
            return null;
        }
        PartyFormation result = null;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.split(",");
                if (p[0].equals(matchId)) {
                    result = new PartyFormation(p[0], Integer.parseInt(p[1]), PartyFormation.Status.valueOf(p[2]));
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}