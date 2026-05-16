package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;
import java.util.List;
import java.util.ArrayList;

public class Sequence extends BtNode {
    private List<BtNode> children = new ArrayList<>();

    public Sequence(List<BtNode> children) {
        this.children = children;
    }

    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        boolean anyChildRunning = false;

        for (BtNode node : children) {
            switch (node.evaluate(world, entityID)) {
                case FAILURE:
                    state = NodeState.FAILURE;
                    return state;
                case SUCCESS:
                    continue;
                case RUNNING:
                    anyChildRunning = true;
                    continue;
                default:
                    state = NodeState.SUCCESS;
                    return state;
            }
        }

        state = anyChildRunning ? NodeState.RUNNING : NodeState.SUCCESS;
        return state;
    }
}

