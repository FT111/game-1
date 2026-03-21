package engine_interfaces.objects.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;

public class CollisionEvent extends Event {
    public EntityID entityId;

    public CollisionEvent(EntityID entityId) {
        this.entityId = entityId;
    }

}
