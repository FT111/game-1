package engine_interfaces.objects;

import java.util.UUID;

/// Event bus event
public abstract class Event {
    public UUID eventID = UUID.randomUUID();
//    private final long tick;


//    public long getTick() {
//        return tick;
//    }

    // For serialization
//    @Override
//    public String toString() {
//        return "event-"+getClass().getSimpleName()+"-t" + tick;
//    }
}