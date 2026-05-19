package resources.objectives;

import engine.EventBus;
import engine.World;

public interface ObjectiveRule {
    boolean isMet(World world, EventBus eventBus);
    String getDescription();
}
