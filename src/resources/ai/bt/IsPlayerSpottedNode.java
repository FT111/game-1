package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;
import resources.GuardAiSystem;
import engine_interfaces.objects.Point;

public class IsPlayerSpottedNode extends BtNode {
    private final GuardAiSystem system;

    public IsPlayerSpottedNode(GuardAiSystem system) {
        this.system = system;
    }

    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        Point knownPos = system.getLastKnownPlayerPos(entityID);
        if (knownPos != null) {
            state = NodeState.SUCCESS;
            return state;
        }

        state = NodeState.FAILURE;
        return state;
    }
}
