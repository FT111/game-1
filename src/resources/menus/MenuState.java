package resources.menus;

import engine_interfaces.objects.LayerID;

import java.util.*;

public abstract class MenuState {

    private final Set<LayerID> layers = new LinkedHashSet<>();
    private final Map<LayerID, Runnable> clickBindings = new HashMap<>();
    private final Map<LayerID, Runnable> hoverBindings = new HashMap<>();
    private final Map<LayerID, Runnable> hoverExitBindings = new HashMap<>();
    private final Map<KeyInputBind, Runnable> keyPressBindings = new HashMap<>();

    protected void show(LayerID layer) {
        layers.add(layer);
    }

    protected void bindClick(LayerID layer, Runnable callback) {
        clickBindings.put(layer, callback);
    }
    protected void bindHoverEnter(LayerID layer, Runnable callback) {
        hoverBindings.put(layer, callback);
    }
    protected void bindHoverExit(LayerID layer, Runnable callback) {
        hoverExitBindings.put(layer, callback);
    }
    protected void bindClick(KeyInputBind keyBind, Runnable callback) {
        keyPressBindings.put(keyBind, callback);
    }

    public Set<LayerID> getLayers() { return Collections.unmodifiableSet(layers); }
    public Map<LayerID, Runnable> getClickBindings() { return Collections.unmodifiableMap(clickBindings); }
    public Map<LayerID, Runnable> getHoverEnterBindings() { return Collections.unmodifiableMap(hoverBindings); }
    public Map<LayerID, Runnable> getHoverExitBindings() { return Collections.unmodifiableMap(hoverExitBindings); }
    public Map<KeyInputBind, Runnable> getKeyPressBindings() { return Collections.unmodifiableMap(keyPressBindings); }
}