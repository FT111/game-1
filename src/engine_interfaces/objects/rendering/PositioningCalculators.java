package engine_interfaces.objects.rendering;

import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.components.ParentComponent;
import engine_interfaces.objects.components.PositionComponent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PositioningCalculators {
    @FunctionalInterface
    public interface PositioningStrategy {
        Point calculatePosition(Point currentPosition, LayerID layer, World world, CameraView camera);
    }

    // Use the scene graph to calculate accumulated offset through parents
    public static Point findRelativeOffset(World world, LayerID id) {
        var node = world.layerSceneGraphNodes.get(id);
        if (node == null) {
            return new Point(0, 0);
        }
        Point offset = new Point(0, 0);
        while (node.parent != null) {
            // Add the position of every layer in the path to the root to the offset
            HashMap<Class<? extends Component>, Component> parentComponents = world.Layers.get(node.parent.objectId);
            if (parentComponents == null) { return offset; }

            var nodeParentPosition = (PositionComponent) parentComponents.get(PositionComponent.class);
            // If it doesn't have a position, skip it
            if (nodeParentPosition == null) { node = node.parent; continue; }

            offset = nodeParentPosition.Origin.add(offset);
            node = node.parent;
        }

        return offset;
    }
    public static Map<Positioning, PositioningStrategy> calc = new EnumMap<>(Map.of(
            Positioning.ABSOLUTE, (currentPosition, layerId, world, camera) ->
                    camera.worldToScreen(currentPosition),
            Positioning.RELATIVE, (currentPosition, layerId, world, camera) ->
                    camera.worldToScreen(currentPosition.add(findRelativeOffset(world, layerId))),
            Positioning.FIXED, (currentPosition, layerId, world, camera) ->
                    currentPosition.add(findRelativeOffset(world,layerId))
    ));
}
