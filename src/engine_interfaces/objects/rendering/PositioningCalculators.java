package engine_interfaces.objects.rendering;

import engine.CoreWorld;
import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.ParentComponent;
import engine_interfaces.objects.components.PositionComponent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PositioningCalculators {

    // Calculates the child's offset relative to its immediate parent's dimensions
    public static Point calculateAlignmentOffset(World world, Point position, LayerID childId, CameraView camera) {
        HashMap<Class<? extends Component>, Component> childComps = world.Layers.get(childId);
        if (childComps == null) return new Point(0, 0);

        PositionComponent childPos = CoreWorld.getComponent(childComps, PositionComponent.class);
        DimensionsComponent childDim = CoreWorld.getComponent(childComps, DimensionsComponent.class);

        int parentWidth, parentHeight;

        ParentComponent parentComponent = CoreWorld.getComponent(childComps, ParentComponent.class);
        if (parentComponent != null && parentComponent.parentLayerId != null) {
            // Has a parent, align to container
            DimensionsComponent parentDim = world.getLayerComponent(parentComponent.parentLayerId, DimensionsComponent.class);
            parentWidth = parentDim != null ? parentDim.width : 0;
            parentHeight = parentDim != null ? parentDim.height : 0;
        } else {
            // No parent, treat the Camera/Window as the root container
            parentWidth = camera.width;
            parentHeight = camera.height;
        }

        int dx = 0;
        int dy = 0;

        // Horizontal Alignment
        switch (childPos.alignment) {
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> dx = (parentWidth - childDim.width) / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> dx = parentWidth - childDim.width;
        }

        // Vertical Alignment
        switch (childPos.alignment) {
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> dy = (parentHeight - childDim.height) / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> dy = parentHeight - childDim.height;
        }

        // Return the anchored position + the local origin offset (which acts as margin)
        return new Point(dx + position.x(), dy + position.y());
    }

    public static Point getPositionFromHierarchy(World world, LayerID id, CameraView camera) {
        var comps = world.Layers.get(id);
        if (comps != null) {
            ParentComponent pComp = CoreWorld.getComponent(comps, ParentComponent.class);
            if (pComp != null && pComp.parentLayerId != null) {
                var parentComps = world.Layers.get(pComp.parentLayerId);
                if (parentComps != null) {
                    PositionComponent parentPos = CoreWorld.getComponent(parentComps, PositionComponent.class);
                    if (parentPos != null) {
                        return calc.get(parentPos.positionStrategy).calculatePosition(parentPos.Origin, pComp.parentLayerId, world, camera);
                    }
                }
            }
        }
        return new Point(0, 0);
    }

    @FunctionalInterface
    public interface PositioningStrategy {
        Point calculatePosition(Point currentPosition, LayerID layer, World world, CameraView camera);
    }

    public static Map<Positioning, PositioningStrategy> calc = new EnumMap<>(Map.of(
            Positioning.ABSOLUTE, (currentPosition, layerId, world, camera) ->
                    camera.worldToScreen(calculateAlignmentOffset(world, currentPosition, layerId, camera)),

            Positioning.RELATIVE, (currentPosition, layerId, world, camera) -> {
                Point localOffset = calculateAlignmentOffset(world, currentPosition, layerId, camera);
                var comps = world.Layers.get(layerId);
                if (comps != null && comps.containsKey(ParentComponent.class)) {
                    return getPositionFromHierarchy(world, layerId, camera).add(localOffset);
                }
                return camera.worldToScreen(localOffset);
            },

            Positioning.FIXED, (currentPosition, layerId, world, camera) -> {
                Point localOffset = calculateAlignmentOffset(world, currentPosition, layerId, camera);
                var comps = world.Layers.get(layerId);
                if (comps != null && comps.containsKey(ParentComponent.class)) {
                    return getPositionFromHierarchy(world, layerId, camera).add(localOffset);
                }
                return localOffset;
            }
    ));}
