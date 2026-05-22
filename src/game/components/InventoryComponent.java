package game.components;

import engine_interfaces.objects.Component;

import java.util.HashMap;

public class InventoryComponent extends Component {
    public HashMap<String, Item> items = new HashMap<>();

    public static class Item {
        public String itemName;
        public String description;

        public Item(String itemName, String description) {
            this.itemName = itemName;
            this.description = description;
        }

        @Override
        public String toString() {
            return itemName;
        }
    }

    public InventoryComponent() {
    }

    public InventoryComponent(InventoryComponent.Item ...items) {
        for (Item item : items) {
            this.items.put(item.itemName, item);
        }
    }
}
