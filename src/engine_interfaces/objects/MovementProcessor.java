package engine_interfaces.objects;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.events.MovementProposalEvent;

public interface MovementProcessor {
    boolean validateMove(MovementProposalEvent proposal);
}
