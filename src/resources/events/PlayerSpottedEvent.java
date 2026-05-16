package resources.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.Point;

public class PlayerSpottedEvent extends Event {
    public final EntityID spotterId;
    public final EntityID playerEntityId;
    public final Point playerPosition;

    public PlayerSpottedEvent(EntityID spotterId, EntityID playerEntityId, Point playerPosition) {
        this.spotterId = spotterId;
        this.playerEntityId = playerEntityId;
        this.playerPosition = playerPosition;
    }
}

