package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;

public class LayerHoverExitEvent extends Event {
    public LayerID layerId;

    public LayerHoverExitEvent(LayerID layerId) {
        this.layerId = layerId;
    }
}
