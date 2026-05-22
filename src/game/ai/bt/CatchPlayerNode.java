package game.ai.bt;

import engine.Logs;
import engine.World;
import engine_interfaces.objects.EntityID;
import game.GuardAiSystem;
import game.events.PlayerCaughtEvent;

public class CatchPlayerNode extends BtNode {
    private final GuardAiSystem system;

    public CatchPlayerNode(GuardAiSystem system) {
        this.system = system;
    }
    @Override
    public NodeState evaluate(World world, EntityID entityID) {
        Logs.log("Player caught by guard " + entityID);
        this.system.bus.publish(new PlayerCaughtEvent());
        state = NodeState.SUCCESS;
        return state;
    }
}
