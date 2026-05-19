package resources.components;

import engine_interfaces.objects.Component;

import java.util.ArrayList;
import java.util.List;

public class InventoryComponent extends Component {
    public List<Item> items = new ArrayList<>();

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
}
