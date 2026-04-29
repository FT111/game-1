package resources.menus;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface InteractionHook {
    void apply(LayerID layerID, Consumer<Map<Class<? extends Component>, Component>> callback);
}
