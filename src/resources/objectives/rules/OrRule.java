package resources.objectives.rules;

import engine.EventBus;
import engine.World;
import resources.objectives.ObjectiveRule;

public class OrRule implements ObjectiveRule {
    private final ObjectiveRule rule1;
    private final ObjectiveRule rule2;
    private final String description;

    public OrRule(ObjectiveRule rule1, ObjectiveRule rule2) {
        this.rule1 = rule1;
        this.rule2 = rule2;
        this.description = rule1.getDescription() + " OR " + rule2.getDescription();
    }

    public OrRule(ObjectiveRule rule1, ObjectiveRule rule2, String descriptionOverride) {
        this.rule1 = rule1;
        this.rule2 = rule2;
        this.description = descriptionOverride;
    }

    @Override
    public boolean isMet(World world, EventBus eventBus) {
        return rule1.isMet(world, eventBus) || rule2.isMet(world, eventBus);
    }

    @Override
    public String getDescription() {
        return description;
    }
}
