package engine_interfaces.objects;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.events.MovementProposalEvent;

import java.util.HashMap;

public interface MovementProcessor {
    boolean validateMove(HashMap<EntityID, HashMap<Class<? extends Component>, Component>> entityState, int tickCount, MovementProposalEvent proposal);
}
