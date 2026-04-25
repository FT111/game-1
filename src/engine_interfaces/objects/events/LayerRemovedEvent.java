package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;

public class LayerRemovedEvent extends Event {
    public LayerID id;

    public LayerRemovedEvent(LayerID id) {
        this.id = id;
    }
}

