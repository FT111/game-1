package engine;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// Contains all entities and layers in the game world.
/// Responsible for world data, but not logic.
public class World {
    public HashMap<EntityID, HashMap<Class<? extends Component>, ? extends Component>> Entities = new HashMap<>();
    public HashMap<LayerID, HashMap<Class<? extends Component>, ? extends Component>> Layers = new HashMap<>();

    public HashMap<Class<Component>, List<EntityID>> ComponentEntitiesIndex = new HashMap<>();
    public HashMap<Class<Component>, List<LayerID>> ComponentLayersIndex = new HashMap<>();
}
