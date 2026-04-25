package engine;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.scene.SceneNode;

import java.util.HashMap;
import java.util.UUID;

public abstract class CoreWorld {
    public HashMap<EntityID, HashMap<Class<? extends Component>, Component>> Entities = new HashMap<>();
    public HashMap<LayerID, HashMap<Class<? extends Component>, Component>> Layers = new HashMap<>();

    public SceneNode<LayerID> layerSceneGraphRoot = new SceneNode<>(null, null);
    public HashMap<LayerID, SceneNode<LayerID>> layerSceneGraphNodes = new HashMap<>();

    public EntityID createEntity(String Id) {
        EntityID newId = new EntityID(Id);
        Entities.put(newId, new HashMap<>());
        onEntityCreated(newId);
        return newId;
    }

    public EntityID createEntity() {
        return createEntity(UUID.randomUUID().toString());
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
        onLayerCreated(newId);
        return newId;
    }

    public LayerID createLayer() {
        return createLayer(UUID.randomUUID().toString());
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
        onComponentAddedToEntity(entityId, component);
    }

    public void addComponentToLayer(LayerID layerId, Component component) {
        HashMap<Class<? extends Component>,  Component> components = Layers.get(layerId);
        if (components == null) {
            throw new IllegalArgumentException("Layer ID does not exist: " + layerId);
        }
        components.put(component.getClass(), component);
        onComponentAddedToLayer(layerId, component);
    }

    public void removeEntity(EntityID entityId) {
        if (!Entities.containsKey(entityId)) {
            throw new IllegalArgumentException("Entity ID does not exist: " + entityId);
        }
        Entities.remove(entityId);
        onEntityRemoved(entityId);
    }

    public void removeLayer(LayerID layerId) {
        if (!Layers.containsKey(layerId)) {
            throw new IllegalArgumentException("Layer ID does not exist: " + layerId);
        }
        onLayerRemoved(layerId);
        Layers.remove(layerId);
    }

    public void merge(CoreWorld other) {
        other.Entities.forEach((entityId, components) -> {
            if (Entities.containsKey(entityId)) {
                throw new IllegalArgumentException("Entity ID already exists in this world: " + entityId);
            }
            Entities.put(entityId, components);
            onEntityCreated(entityId);
//            components.forEach((componentClass, component) -> onComponentAddedToEntity(entityId, component));
        });

        other.Layers.forEach((layerId, components) -> {
            if (Layers.containsKey(layerId)) {
                throw new IllegalArgumentException("Layer ID already exists in this world: " + layerId);
            }
            Layers.put(layerId, components);
            onLayerCreated(layerId);
//            components.forEach((componentClass, component) -> onComponentAddedToLayer(layerId, component));
        });
    }

    public void remove(CoreWorld other) {
        other.Entities.keySet().forEach(entityId -> {
            if (Entities.containsKey(entityId)) {
                removeEntity(entityId);
            }
        });
        other.Layers.keySet().forEach(layerId -> {
            if (Layers.containsKey(layerId)) {
                removeLayer(layerId);
            }
        });
    }

    protected abstract void onEntityCreated(EntityID entityId);
    protected abstract void onLayerCreated(LayerID layerId);
    protected abstract void onEntityRemoved(EntityID entityId);
    protected abstract void onLayerRemoved(LayerID layerId);
    protected abstract void onComponentAddedToEntity(EntityID entityId, Component component);
    protected abstract void onComponentAddedToLayer(LayerID layerId, Component component);
}

