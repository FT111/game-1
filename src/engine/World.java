package engine;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.ComponentIndex;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.events.EntityRegisteredEvent;
import engine_interfaces.objects.events.LayerRegisteredEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/// Contains all entities and layers in the game world.
/// Responsible for world data, but not logic.
public class World {
    public HashMap<EntityID, HashMap<Class<? extends Component>, Component>> Entities = new HashMap<>();
    public HashMap<LayerID, HashMap<Class<? extends Component>, Component>> Layers = new HashMap<>();
    private EventBus bus;

    public World(EventBus bus) {
        this.bus = bus;
    }

    public EntityID createEntity(String Id) {
        EntityID newId = new EntityID(Id);
        Entities.put(newId, new HashMap<>());
        bus.publish(new EntityRegisteredEvent(newId));
        return newId;
    }

    public EntityID createEntity() {
        String newId = UUID.randomUUID().toString();
        return createEntity(newId);
    }

    public EntityID createEntity(String Id, Component... components) {
        EntityID newId = createEntity(Id);
        for (Component component : components) {
            addComponentToEntity(newId, component);
        }
        return newId;
    }

    public EntityID createEntity(Component... components) {
        EntityID newId = createEntity();
        for (Component component : components) {
            addComponentToEntity(newId, component);
        }
        return newId;
    }

    public LayerID createLayer(String Id) {
        LayerID newId = new LayerID(Id);
        Layers.put(newId, new HashMap<>());
        bus.publish(new LayerRegisteredEvent(newId));
        return newId;
    }

    public LayerID createLayer() {
        String newId = UUID.randomUUID().toString();
        return createLayer(newId);
    }

    public LayerID createLayer(String Id, Component... components) {
        LayerID newId = createLayer(Id);
        for (Component component : components) {
            addComponentToLayer(newId, component);
        }
        return newId;
    }

    public LayerID createLayer(Component... components) {
        LayerID newId = createLayer();
        for (Component component : components) {
            addComponentToLayer(newId, component);
        }
        return newId;
    }

    public void addComponentToEntity(EntityID entityId, Component component) {
        HashMap<Class<? extends Component>, Component> components = Entities.get(entityId);
        if (components == null) {
            throw new IllegalArgumentException("Entity ID does not exist: " + entityId);
        }
        components.put(component.getClass(), component);
        ComponentEntitiesIndex.add(entityId, component.getClass());
    }

    public void addComponentToLayer(LayerID layerId, Component component) {
        HashMap<Class<? extends Component>,  Component> components = Layers.get(layerId);
        if (components == null) {
            throw new IllegalArgumentException("Layer ID does not exist: " + layerId);
        }
        components.put(component.getClass(), component);
        ComponentLayersIndex.add(layerId, component.getClass());
    }

    public ComponentIndex<EntityID> ComponentEntitiesIndex = new ComponentIndex<>();
    public ComponentIndex<LayerID> ComponentLayersIndex = new ComponentIndex<>();
}
