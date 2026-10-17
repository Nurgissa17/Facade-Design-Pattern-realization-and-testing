package matchmaking.subsystem.a;
public class PartyFormation {
    public enum Status {
        NEW, FORMED, CANCELLED
    }
    private final String matchId;
    private final int averageRating;
    private Status status;
    public PartyFormation(String matchId, int averageRating, Status status) {
        this.matchId = matchId;
        this.averageRating = averageRating;
        this.status = status;
    }
    public void form() {
        if (status != Status.NEW) {
            throw new IllegalStateException();
        }
        status = Status.FORMED;
    }
    public void cancel() {
        if (status != Status.FORMED) {
            throw new IllegalStateException();
        }
        status = Status.CANCELLED;
    }
    public String getMatchId() {
        return matchId;
    }
    public int getAverageRating() {
        return averageRating;
    }
    public Status getStatus() {
        return status;
    }
}