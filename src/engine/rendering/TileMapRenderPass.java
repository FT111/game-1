package engine.rendering;

import engine.CoreWorld;
import engine.Utils;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import resources.components.MaskToTileMapComponent;
import engine_interfaces.objects.rendering.*;

import java.util.HashMap;
import java.util.HashSet;

public class TileMapRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        HashSet<LayerID> tileMapEntities = renderObjects.world().ComponentLayersIndex.query(
                TileMapComponent.class,
                PositionComponent.class,
                DimensionsComponent.class,
                VisibilityComponent.class
        );

        tileMapEntities.forEach(layerID -> {
            // Get the tile map and position components for this layer
            HashMap<Class<? extends Component>, Component> components = renderObjects.world().Layers.get(layerID);
            TileMapComponent tileMapComponent = CoreWorld.getComponent(components, TileMapComponent.class);
            PositionComponent positionComponent = CoreWorld.getComponent(components, PositionComponent.class);
            DimensionsComponent dimensionsComponent = CoreWorld.getComponent(components, DimensionsComponent.class);
            VisibilityComponent visibilityComponent = CoreWorld.getComponent(components, VisibilityComponent.class);

            if (!visibilityComponent.isVisible) {
                return;
            }

            Cell[][] tileMap = renderObjects.resources().getAsset(tileMapComponent.resourceId, tileMapComponent.assetId, Cell[][].class);
            Point tileEndPoint = new Point(positionComponent.Origin.x() + tileMap[0].length, positionComponent.Origin.y() + tileMap.length);
            // Cull using the world's coordinates for non-fixed layers
            if (!RenderPassUtils.isLayerInView(renderObjects, positionComponent, dimensionsComponent)) {
                return;
            }

            var screenTileMapOrigin = renderObjects.layoutManager().getCalculatedScreenPosition(layerID, renderObjects.camera());
            if (screenTileMapOrigin == null) return;

            // Render the tile map to the buffer, culling individual cells that are not in view of the camera
            for (int y = 0; y < dimensionsComponent.height; y++) {
                for (int x = 0; x < dimensionsComponent.width; x++) {
                    // Check bounds of tile map asset
                    if (y >= tileMap.length || x >= tileMap[y].length) {
                        continue;
                    }

                    Cell cell = tileMap[y][x];
                    if (cell == null) {
                        continue;
                    }

                    // If this layer is masked by a vision layer, only render cells that are marked visible
                    MaskToTileMapComponent visionMask = CoreWorld.getComponent(components, MaskToTileMapComponent.class);
                    int worldX = positionComponent.Origin.x() + x;
                    int worldY = positionComponent.Origin.y() + y;
                    if (!RenderPassUtils.isPointVisibleInVisionMask(renderObjects, visionMask, worldX, worldY)) {
                        continue;
                    }

                    cell.zIndex = positionComponent.zIndex;
                    var screenPoint = new Point(screenTileMapOrigin.x() + x, screenTileMapOrigin.y() + y);
                    if (!renderObjects.camera().isScreenPointInView(screenPoint)) {
                        continue;
                    }

                    // safe write to buffer (handles collation + bounds)
                    RenderPassUtils.trySetCell(buffer, screenPoint.x(), screenPoint.y(), cell);
                }
            }
        });
    }

    //    public static void debugPrintTileMap(TileMapComponent tileMapComponent, Cell[][] tileMapAsset) {
//        // IO.println("Debug: Printing tile map for layer " + tileMapComponent.assetId);
//        for (int y = 0; y < tileMapComponent.width; y++) {
//            StringBuilder row = new StringBuilder();
//            for (int x = 0; x < dimensions.height; x++) {
//                if (y >= tileMapAsset.length || x >= tileMapAsset[y].length) {
//                    row.append(' ');
//                    continue;
//                }
//                Cell cell = tileMapAsset[y][x];
//                if (cell == null) {
//                    row.append(' ');
//                } else {
//                    row.append(cell.content);
//                }
//            }
//            // IO.println(row.toString());
//        }
//    }
}
