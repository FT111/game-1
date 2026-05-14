package engine.layout;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine.Resources;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;

public interface MapLayerDefinition {
    boolean matches(HashMap<Class<? extends Component>, Component> components);
    HashSet<Point> extractPoints(HashMap<Class<? extends Component>, Component> components, Resources resources);
}
