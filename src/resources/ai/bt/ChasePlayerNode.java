package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import resources.GuardAiSystem;
import resources.components.GuardComponent;
import java.util.List;
import java.util.Stack;

public class ChasePlayerNode extends BtNode {
    private final GuardAiSystem system;

    public ChasePlayerNode(GuardAiSystem system) {
        this.system = system;
    }

    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        Point targetPoint = system.getLastKnownPlayerPos(entityID);
        if (targetPoint == null) {
            return NodeState.FAILURE;
        }

        PositionComponent positionComp = (PositionComponent) world.Entities.get(entityID).get(PositionComponent.class);
        GuardComponent guardComp = (GuardComponent) world.Entities.get(entityID).get(GuardComponent.class);

        if (positionComp == null || guardComp == null) return NodeState.FAILURE;

        if (positionComp.Origin.equals(targetPoint)) {
            // Reached last known position
            system.clearLastKnownPlayerPos(entityID);
            guardComp.interpolatedPathPoints.clear();
            return NodeState.SUCCESS;
        }

        // Re-route towards player if we don't have a path, or if we need to update it
        if (guardComp.interpolatedPathPoints == null || guardComp.interpolatedPathPoints.isEmpty() || tickNeedsReroute(world)) {
            List<Point> newRoute = system.routeBetween(positionComp.Origin, targetPoint);
            guardComp.interpolatedPathPoints = new Stack<>();
            for (int i = newRoute.size() - 1; i >= 0; i--) {
                guardComp.interpolatedPathPoints.push(newRoute.get(i));
            }
        }

        if (!guardComp.interpolatedPathPoints.isEmpty() && system.getPendingMovement(entityID) == null) {
            Point nextPoint = guardComp.interpolatedPathPoints.pop();
            system.proposeMovement(entityID, positionComp.Origin, nextPoint);
        }

        return NodeState.RUNNING;
    }

    private boolean tickNeedsReroute(World world) {
        // Just an abstraction so they don't route continuously on every frame;
        // Can be improved later. For now, true if stack matches exact
        return false;
    }
}
