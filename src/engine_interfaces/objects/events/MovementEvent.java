package engine_interfaces.objects.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.Point;

public class MovementEvent extends Event {
    public EntityID entityID;
    public Point previousOrigin;
    public Point newOrigin;

    public MovementEvent(EntityID entityID, Point previousOrigin, Point newOrigin) {
        this.entityID = entityID;
        this.previousOrigin = previousOrigin;
        this.newOrigin = newOrigin;
    }

}
