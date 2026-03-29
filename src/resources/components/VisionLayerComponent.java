package resources.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;

public class VisionLayerComponent extends Component {
    public EntityID emitter;

    public VisionLayerComponent(EntityID emitter) {
        this.emitter = emitter;
    }
}
