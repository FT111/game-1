package resources.ai.bt;

import engine.World;
import engine_interfaces.objects.EntityID;
import resources.GuardAiSystem;
import resources.events.PlayerCaughtEvent;

public class CatchPlayerNode extends BtNode {
    private final GuardAiSystem system;

    public CatchPlayerNode(GuardAiSystem system) {
        this.system = system;
    }
    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        this.system.bus.publish(new PlayerCaughtEvent());
        state = NodeState.SUCCESS;
        return state;
    }
}
