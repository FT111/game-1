package resources.objectives.rules;

import engine.EventBus;
import engine.World;
import resources.objectives.ObjectiveRule;

public class HasItemRule implements ObjectiveRule {
    private final String itemName;
    private final String description;

    // For now we simulate finding the item
    private boolean itemFoundDummy = false;

    public HasItemRule(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }

    @Override
    public boolean isMet(World world, EventBus eventBus) {
        // TODO: implement actual inventory check once InventorySystem is built
        return itemFoundDummy;
    }

    public void setItemFound(boolean found) {
        this.itemFoundDummy = found;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
