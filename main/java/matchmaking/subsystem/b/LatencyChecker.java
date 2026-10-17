package matchmaking.subsystem.b;
public class LatencyChecker {
    public boolean isAcceptable(int latencyMs) {
        return latencyMs > 0 && latencyMs <= 100;
    }
}
