package resources.menus;

import engine_interfaces.objects.LayerID;

import java.util.*;

public abstract class MenuState {

    private final Set<LayerID> layers = new LinkedHashSet<>();
    private final Map<LayerID, Runnable> clickBindings = new HashMap<>();
    private final Map<KeyInputBind, Runnable> keyPressBindings = new HashMap<>();

    protected void show(LayerID layer) {
        layers.add(layer);
    }

    protected void bind(LayerID layer, Runnable callback) {
        clickBindings.put(layer, callback);
    }
    protected void bind(KeyInputBind keyBind, Runnable callback) {
        keyPressBindings.put(keyBind, callback);
    }

    public Set<LayerID> getLayers() { return Collections.unmodifiableSet(layers); }
    public Map<LayerID, Runnable> getClickBindings() { return Collections.unmodifiableMap(clickBindings); }
    public Map<KeyInputBind, Runnable> getKeyPressBindings() { return Collections.unmodifiableMap(keyPressBindings); }
}