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

        if (guardComp.mainPathPoints != null && !guardComp.mainPathPoints.isEmpty()) {
            if (guardComp.mainPathPoints.peek() != null && guardComp.mainPathPoints.peek().equals(positionComp.Origin)) {
                guardComp.mainPathPoints.pop();
            }

            system.routeIfNecessary(guardComp, positionComp);

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

        return NodeState.SUCCESS;
    }
}
