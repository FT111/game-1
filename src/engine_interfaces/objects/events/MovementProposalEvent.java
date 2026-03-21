package engine_interfaces.objects.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.Point;

public class MovementProposalEvent extends Event {
    public EntityID entityID;
    public Point currentPosition;
    public Point proposedPosition;


    public MovementProposalEvent(EntityID id, Point proposedPosition, Point currentPosition) {
        this.entityID = id;
        this.proposedPosition = proposedPosition;
        this.currentPosition = currentPosition;
    }
}
