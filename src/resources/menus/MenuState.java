package resources.menus;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import resources.UiBuilders;

import java.util.*;
import java.util.function.Consumer;

public abstract class MenuState {

    private final Set<LayerID> layers = new LinkedHashSet<>();
    private final Map<LayerID, Consumer<Map<Class<? extends Component>, Component>>> clickBindings = new HashMap<>();
    private final Map<LayerID, Consumer<Map<Class<? extends Component>, Component>>> hoverBindings = new HashMap<>();
    private final Map<LayerID, Consumer<Map<Class<? extends Component>, Component>>> hoverExitBindings = new HashMap<>();
    private final Map<KeyInputBind, Runnable> keyPressBindings = new HashMap<>();

    public Consumer<LayerID> onLayerShown;
    public Consumer<LayerID> onLayerHidden;

    protected void configureUiBuilder(UiBuilders ui) {
        ui.setInteractionHooks(new InteractionHooks(
                this::bindClick,
                this::bindHoverEnter,
                this::bindHoverExit
        ));
    }

    public MenuState(StateContext ctx) {
        configureUiBuilder(ctx.ui());
    }

    protected void show(LayerID layer) {
        layers.add(layer);

        if (onLayerShown != null) {
            onLayerShown.accept(layer);
        }
    }

    protected void hide(LayerID layer) {
        layers.remove(layer);

        if (onLayerHidden != null) {
            onLayerHidden.accept(layer);
        }
    }

    protected void bindClick(LayerID layer, Consumer<Map<Class<? extends Component>, Component>> callback) {
        clickBindings.put(layer, callback);
    }
    protected void bindHoverEnter(LayerID layer, Consumer<Map<Class<? extends Component>, Component>> callback) {
        hoverBindings.put(layer, callback);
    }
    protected void bindHoverExit(LayerID layer, Consumer<Map<Class<? extends Component>, Component>> callback) {
        hoverExitBindings.put(layer, callback);
    }
    protected void bindKeypress(KeyInputBind keyBind, Runnable callback) {
        keyPressBindings.put(keyBind, callback);
    }

    public Set<LayerID> getLayers() { return Collections.unmodifiableSet(layers); }
    public Map<LayerID, Consumer<Map<Class<? extends Component>, Component>>> getClickBindings() { return Collections.unmodifiableMap(clickBindings); }
    public Map<LayerID, Consumer<Map<Class<? extends Component>, Component>>> getHoverEnterBindings() { return Collections.unmodifiableMap(hoverBindings); }
    public Map<LayerID, Consumer<Map<Class<? extends Component>, Component>>> getHoverExitBindings() { return Collections.unmodifiableMap(hoverExitBindings); }
    public Map<KeyInputBind, Runnable> getKeyPressBindings() { return Collections.unmodifiableMap(keyPressBindings); }
}