package engine_interfaces.objects.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;

public class CollisionEvent extends Event {
    public LayerID collidedWithId;
    public EntityID entityId;

    public CollisionEvent(EntityID entityId, LayerID collidedWithId) {
        this.entityId = entityId;
        this.collidedWithId = collidedWithId;
    }

}
