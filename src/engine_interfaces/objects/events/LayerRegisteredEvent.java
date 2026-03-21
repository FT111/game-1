package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;

public class LayerRegisteredEvent extends Event {
    public LayerID id;

    public LayerRegisteredEvent(LayerID id) {
        this.id = id;
    }
}
