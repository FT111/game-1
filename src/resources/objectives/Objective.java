package resources.objectives;

import engine.EventBus;
import engine.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Objective {
    private final String name;
    private final List<ObjectiveRule> rules;
    private boolean isCompleted = false;

    public Objective(String name, ObjectiveRule... rules) {
        this.name = name;
        this.rules = new ArrayList<>(Arrays.asList(rules));
    }

    public void update(World world, EventBus eventBus) {
        if (isCompleted) return;

        boolean allMet = true;
        for (ObjectiveRule rule : rules) {
            if (!rule.isMet(world, eventBus)) {
                allMet = false;
                break;
            }
        }
        if (allMet && !rules.isEmpty()) {
            isCompleted = true;
        }
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getName() {
        return name;
    }

    public List<ObjectiveRule> getRules() {
        return rules;
    }
}
