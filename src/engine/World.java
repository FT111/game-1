package engine;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.ComponentIndex;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
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
        bus.publish(new EntityRegisteredEvent(entityId));
    }

    @Override
    protected void onLayerCreated(LayerID layerId) {
        bus.publish(new LayerRegisteredEvent(layerId));
    }

    @Override
    protected void onEntityRemoved(EntityID entityId) {
        Entities.get(entityId).keySet().forEach(componentClass -> ComponentEntitiesIndex.remove(entityId, componentClass));
    }

    @Override
    protected void onLayerRemoved(LayerID layerId) {
        Layers.get(layerId).keySet().forEach(componentClass -> ComponentLayersIndex.remove(layerId, componentClass));
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
