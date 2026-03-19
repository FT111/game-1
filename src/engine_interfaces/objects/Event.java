package engine_interfaces.objects;

/// Event bus event
public abstract class Event {
    private final long tick;

    protected Event (long tick) {
        assert tick >= 0 : "Event tick must be non-negative";
        this.tick = tick;
    }

    public long getTick() {
        return tick;
    }

    // For serialization
    @Override
    public String toString() {
        return "event-"+getClass().getSimpleName()+"-t" + tick;
    }
}