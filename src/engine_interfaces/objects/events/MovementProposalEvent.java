package engine_interfaces.objects.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.Point;

import java.util.UUID;

public class MovementProposalEvent extends Event {
    public EntityID entityID;
    public Point currentPosition;
    public Point proposedPosition;
    public boolean retryOnRejection = false;

    public UUID dependsOnMovement;



    public MovementProposalEvent(EntityID id, Point proposedPosition, Point currentPosition) {
        this.entityID = id;
        this.proposedPosition = proposedPosition;
        this.currentPosition = currentPosition;
    }

    public MovementProposalEvent(EntityID id, Point proposedPosition, Point currentPosition, UUID dependsOnMovement) {
        this.entityID = id;
        this.proposedPosition = proposedPosition;
        this.currentPosition = currentPosition;
        this.dependsOnMovement = dependsOnMovement;
    }
}
