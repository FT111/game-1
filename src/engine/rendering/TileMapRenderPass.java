package engine.rendering;

import engine.Utils;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.rendering.*;

import java.util.HashMap;
import java.util.HashSet;

public class TileMapRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        HashSet<LayerID> tileMapEntities = (HashSet<LayerID>) renderObjects.world().ComponentLayersIndex.query(new Class[]{TileMapComponent.class, PositionComponent.class, DimensionsComponent.class, VisibilityComponent.class});

        tileMapEntities.forEach(layerID -> {
            // Get the tile map and position components for this layer
            HashMap<Class<? extends Component>, Component> components = renderObjects.world().Layers.get(layerID);
            TileMapComponent tileMapComponent = (TileMapComponent) components.get(TileMapComponent.class);
            PositionComponent positionComponent = (PositionComponent) components.get(PositionComponent.class);
            DimensionsComponent dimensionsComponent = (DimensionsComponent) components.get(DimensionsComponent.class);
            VisibilityComponent visibilityComponent = (VisibilityComponent) components.get(VisibilityComponent.class);

            if (!visibilityComponent.isVisible) {
                return;
            }

            Cell[][] tileMap = renderObjects.resources().getAsset(tileMapComponent.resourceId, tileMapComponent.assetId, Cell[][].class);
            Point tileEndPoint = new Point(positionComponent.Origin.x() + tileMap[0].length, positionComponent.Origin.y() + tileMap.length);
            // Cull tile map if it is not in view of the camera and a world-positioned object
            if (!positionComponent.positionStrategy.equals(Positioning.FIXED)  && !renderObjects.camera().isWorldPointInView(positionComponent.Origin, tileEndPoint)) {
                return;
            }

            var screenTileMapOrigin = PositioningCalculators.calc.get(positionComponent.positionStrategy).calculatePosition(
                    new Point(positionComponent.Origin.x(), positionComponent.Origin.y()),
                    layerID,
                    renderObjects.world(),
                    renderObjects.camera()
            );

            // Render the tile map to the buffer, culling individual cells that are not in view of the camera
            for (int y = 0; y < dimensionsComponent.width; y++) {
                for (int x = 0; x < dimensionsComponent.height; x++) {
                    // Check bounds of tile map asset
                    if (y >= tileMap.length || x >= tileMap[y].length) {
                        continue;
                    }

                    Cell cell = tileMap[y][x];
                    if (cell == null) {
                        continue;
                    }

                    cell.zIndex = positionComponent.zIndex;
                    var screenPoint = new Point(screenTileMapOrigin.x() + x, screenTileMapOrigin.y() + y);
                    if (!renderObjects.camera().isScreenPointInView(screenPoint)) {
                        continue;
                    }

                    try {
                        Cell existingCell = buffer.cells[screenPoint.y()][screenPoint.x()];

                        cell = Utils.collateCells(existingCell, cell);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // IO.println("Error:  Cell at " + relativePoint + " is out of bounds for the render buffer. Skipping this cell.");
                        continue;
                    }
                    buffer.cells[screenPoint.y()][screenPoint.x()] = cell;
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
