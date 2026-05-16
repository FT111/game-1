package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;
import java.util.List;
import java.util.ArrayList;

public class Selector extends BtNode {
    private List<BtNode> children = new ArrayList<>();

    public Selector(List<BtNode> children) {
        this.children = children;
    }

    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        for (BtNode node : children) {
            switch (node.evaluate(world, entityID)) {
                case FAILURE:
                    continue;
                case SUCCESS:
                    state = NodeState.SUCCESS;
                    return state;
                case RUNNING:
                    state = NodeState.RUNNING;
                    return state;
                default:
                    continue;
            }
        }

        state = NodeState.FAILURE;
        return state;
    }
}

