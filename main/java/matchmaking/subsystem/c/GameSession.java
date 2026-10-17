package matchmaking.subsystem.c;
public class GameSession {
    public enum Status {
        NEW, CONFIRMED, REJECTED
    }
    private final String matchId;
    private Status status;
    public GameSession(String matchId, Status status) {
        this.matchId = matchId;
        this.status = status;
    }
    public void confirm() {
        if (status != Status.NEW) {
            throw new IllegalStateException();
        }
        status = Status.CONFIRMED;
    }
    public void reject() {
        if (status != Status.NEW) {
            throw new IllegalStateException();
        }
        status = Status.REJECTED;
    }
    public String getMatchId() {return matchId;}
    public Status getStatus() {return status;}
}
