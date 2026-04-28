package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;

public class LayerHoverEvent extends Event {
    public LayerID layerId;

    public LayerHoverEvent(LayerID layerId) {
        this.layerId = layerId;
    }
}
