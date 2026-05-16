package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;

public abstract class BtNode {
    protected NodeState state;

    public NodeState getState() {
        return state;
    }

    public abstract NodeState evaluate(World world, EntityID entityID);
}

