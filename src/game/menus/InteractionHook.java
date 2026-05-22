package game.menus;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;

import java.util.Map;
import java.util.function.Consumer;

@FunctionalInterface
public interface InteractionHook {
    void apply(LayerID layerID, Consumer<Map<Class<? extends Component>, Component>> callback);
}
