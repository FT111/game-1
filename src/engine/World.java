package engine;

import engineInterfaces.objects.Component;
import engineInterfaces.objects.EntityID;
import engineInterfaces.objects.LayerID;

import java.util.HashMap;
import java.util.List;

/// Contains all entities and layers in the game world.
/// Responsible for world data, but not logic.
public class World {
    public HashMap<EntityID, List<Component>> Entities = new HashMap<>();
    public HashMap<LayerID, List<Component>> Layers = new HashMap<>();

    public HashMap<Component, List<EntityID>> ComponentEntitiesIndex = new HashMap<>();
    public HashMap<Component, List<LayerID>> ComponentLayersIndex = new HashMap<>();
}
