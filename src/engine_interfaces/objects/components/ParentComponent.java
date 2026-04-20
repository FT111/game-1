package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;

public class ParentComponent extends Component {
    public LayerID parentLayerId;

    public ParentComponent(LayerID parentLayerId) {
        this.parentLayerId = parentLayerId;
    }
}
