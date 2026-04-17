package resources.menus;

import engine_interfaces.objects.LayerID;

import java.util.*;
import java.util.function.Consumer;

public abstract class MenuState {

    private final Set<LayerID> layers = new LinkedHashSet<>();
    private final Map<LayerID, Runnable> bindings = new HashMap<>();

    protected void show(LayerID layer) {
        layers.add(layer);
    }

    protected void bind(LayerID layer, Runnable callback) {
        bindings.put(layer, callback);
    }

    public Set<LayerID> getLayers() { return Collections.unmodifiableSet(layers); }
    public Map<LayerID, Runnable> getBindings() { return Collections.unmodifiableMap(bindings); }
}