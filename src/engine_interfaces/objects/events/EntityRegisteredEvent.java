package engine_interfaces.objects.events;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;

public class EntityRegisteredEvent extends Event {
    public EntityID id;

    public EntityRegisteredEvent(EntityID id) {
        this.id = id;
    }
}
