package game.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;

public class AwarenessChangedEvent extends Event {
    public final EntityID guardId;
    public final EntityID playerId;
    public final int awareness;
    public final int threshold;

    public AwarenessChangedEvent(EntityID guardId, EntityID playerId, int awareness, int threshold) {
        this.guardId = guardId;
        this.playerId = playerId;
        this.awareness = awareness;
        this.threshold = threshold;
    }
}

