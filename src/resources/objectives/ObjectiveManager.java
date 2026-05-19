package resources.objectives;

import engine.EventBus;
import engine.World;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveManager {
    private final List<Objective> objectives = new ArrayList<>();

    public void addObjective(Objective objective) {
        objectives.add(objective);
    }

    public void update(World world, EventBus eventBus) {
        for (Objective obj : objectives) {
            obj.update(world, eventBus);
        }
    }

    public List<Objective> getObjectives() {
        return objectives;
    }
}
