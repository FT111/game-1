package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import resources.GuardAiSystem;

public class IsPlayerCatchable extends BtNode {
    private final GuardAiSystem system;

    public IsPlayerCatchable(GuardAiSystem system) {
        this.system = system;
    }

    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        PositionComponent guardPos = (PositionComponent) world.Entities.get(entityID).get(PositionComponent.class);
        Point playerLastKnownPos = system.getLastKnownPlayerPos(entityID);
        EntityID player = world.ComponentEntitiesIndex.query(PositionComponent.class).stream().findFirst().orElseThrow();
        PositionComponent playerActualPos = (PositionComponent) world.Entities.get(player).get(PositionComponent.class);

        if (playerActualPos.Origin != playerLastKnownPos) {
            return NodeState.FAILURE;
        }

        // check if within 1 cell radius
        if (Math.abs(guardPos.Origin.x() - playerActualPos.Origin.x()) <= 1 && Math.abs(guardPos.Origin.y() - playerActualPos.Origin.y()) <= 1) {
            return NodeState.SUCCESS;
        }
        return NodeState.FAILURE;


    }
}
