package matchmaking.subsystem.b;
public class ServerAllocator {
    public enum Status {
        NEW, ALLOCATED, RELEASED
    }
    private final String matchId;
    private final int latencyMs;
    private Status status;
    public ServerAllocator(String matchId, int latencyMs, Status status) {
        this.matchId = matchId;
        this.latencyMs = latencyMs;
        this.status = status;
    }
    public void allocate() {
        if (status != Status.NEW) {
            throw new IllegalStateException();
        }
        status = Status.ALLOCATED;
    }
    public void release() {
        if (status != Status.ALLOCATED) {
            throw new IllegalStateException();
        }
        status = Status.RELEASED;
    }
    public String getMatchId() {
        return matchId;
    }
    public int getLatencyMs() {
        return latencyMs;
    }
    public Status getStatus() {
        return status;
    }
}