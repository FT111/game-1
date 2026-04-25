package engine;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.ComponentIndex;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.events.LayerRemovedEvent;
import engine_interfaces.objects.events.EntityRegisteredEvent;
import engine_interfaces.objects.events.LayerRegisteredEvent;

/// Contains all entities and layers currently in the game world.
/// Responsible for world data, but not logic.
public class World extends CoreWorld {
    private EventBus bus;

    public ComponentIndex<EntityID> ComponentEntitiesIndex = new ComponentIndex<>();
    public ComponentIndex<LayerID> ComponentLayersIndex = new ComponentIndex<>();

    public World(EventBus bus) {
        this.bus = bus;
    }

    @Override
    protected void onEntityCreated(EntityID entityId) {
        Entities.get(entityId).forEach((componentClass, component) -> ComponentEntitiesIndex.add(entityId, componentClass));
        bus.publish(new EntityRegisteredEvent(entityId));
    }

    @Override
    protected void onLayerCreated(LayerID layerId) {
        Layers.get(layerId).forEach((componentClass, component) -> ComponentLayersIndex.add(layerId, componentClass));
        bus.publish(new LayerRegisteredEvent(layerId));
    }

    @Override
    protected void onEntityRemoved(EntityID entityId) {
        ComponentEntitiesIndex.remove(entityId);
    }

    @Override
    protected void onLayerRemoved(LayerID layerId) {
        ComponentLayersIndex.remove(layerId);
        bus.publish(new LayerRemovedEvent(layerId));
    }

    @Override
    protected void onComponentAddedToEntity(EntityID entityId, Component component) {
        ComponentEntitiesIndex.add(entityId, component.getClass());
    }

    @Override
    protected void onComponentAddedToLayer(LayerID layerId, Component component) {
        ComponentLayersIndex.add(layerId, component.getClass());
    }
}
