package resources.objectives.rules;

import engine.EventBus;
import engine.World;
import resources.objectives.ObjectiveRule;
import resources.components.InventoryComponent;
import resources.components.PlayerComponent;
import engine_interfaces.objects.EntityID;
import java.util.HashSet;

public class HasItemRule implements ObjectiveRule {
    private final String itemName;
    private final String description;

    public HasItemRule(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }

    @Override
    public boolean isMet(World world, EventBus eventBus) {
        HashSet<EntityID> players = world.ComponentEntitiesIndex.query(PlayerComponent.class);
        if (players == null || players.isEmpty()) return false;

        for (EntityID player : players) {
            if (world.Entities.get(player) != null && world.Entities.get(player).containsKey(InventoryComponent.class)) {
                InventoryComponent inv = world.getEntityComponent(player, InventoryComponent.class);
                if (inv != null) {
                    for (InventoryComponent.Item item : inv.items) {
                        if (item.itemName.equals(itemName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
