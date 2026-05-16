package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.components.PositionComponent;
import resources.GuardAiSystem;
import resources.components.GuardComponent;
import engine_interfaces.objects.Point;

import java.util.EmptyStackException;

public class PatrolNode extends BtNode {
    private final GuardAiSystem system;

    public PatrolNode(GuardAiSystem system) {
        this.system = system;
    }

    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        GuardComponent guardComp = (GuardComponent) world.Entities.get(entityID).get(GuardComponent.class);
        PositionComponent positionComp = (PositionComponent) world.Entities.get(entityID).get(PositionComponent.class);

        if (guardComp == null || positionComp == null) {
            return NodeState.FAILURE;
        }

        Point patrolTarget = getCurrentPatrolTarget(guardComp);
        if (patrolTarget == null) {
            return NodeState.SUCCESS;
        }

        if (patrolTarget.equals(positionComp.Origin)) {
            advancePatrolTarget(guardComp);
            guardComp.interpolatedPathPoints.clear();
            patrolTarget = getCurrentPatrolTarget(guardComp);
        }

        system.routeIfNecessary(guardComp, positionComp, patrolTarget);

        if (guardComp.interpolatedPathPoints != null && system.getPendingMovement(entityID) == null) {
            try {
                guardComp.interpolatedPathPoints.peek();
            } catch (EmptyStackException e) {
                return NodeState.SUCCESS; // Finished patrolling
            }

            Point nextPoint = guardComp.interpolatedPathPoints.pop();
            system.proposeMovement(entityID, positionComp.Origin, nextPoint);
        }
        return NodeState.RUNNING;
    }

    private static Point getCurrentPatrolTarget(GuardComponent guardComp) {
        if (guardComp.patrolPoints.isEmpty()) {
            return null;
        }
        int idx = Math.floorMod(guardComp.patrolIndex, guardComp.patrolPoints.size());
        return guardComp.patrolPoints.get(idx);
    }

    private static void advancePatrolTarget(GuardComponent guardComp) {
        if (!guardComp.patrolPoints.isEmpty()) {
            guardComp.patrolIndex = (guardComp.patrolIndex + 1) % guardComp.patrolPoints.size();
        }
    }
}
