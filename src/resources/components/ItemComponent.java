package resources.components;

import engine_interfaces.objects.Component;

public class ItemComponent extends Component {
    public String itemName;
    public String description;

    public ItemComponent(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }
}

