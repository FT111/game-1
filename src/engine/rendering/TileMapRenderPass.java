package engine.rendering;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;
import engine_interfaces.objects.rendering.renderObjects;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class TileMapRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        HashSet tileMapEntities = renderObjects.world().ComponentLayersIndex.query(new Class[] {TileMapComponent.class, PositionComponent.class});

        tileMapEntities.forEach(layerID -> {
            // Get the tile map and position components for this layer
            TileMapComponent tileMapComponent = (TileMapComponent) renderObjects.world().Layers.get(layerID).get(TileMapComponent.class);
            PositionComponent positionComponent = (PositionComponent) renderObjects.world().Layers.get(layerID).get(PositionComponent.class);

            if (!tileMapComponent.isVisible) {
                return;
            }

            Cell[][] tileMap = renderObjects.resources().getAsset(
                    tileMapComponent.resourceId,
                    tileMapComponent.assetId,
                    Cell[][].class);
            Point tileEndPoint = new Point(positionComponent.Origin.x() + tileMap[0].length, positionComponent.Origin.y() + tileMap.length);

//            debugPrintTileMap(tileMapComponent, tileMap);

            // Cull tile map if it is not in view of the camera
            if (!renderObjects.camera().isInView(positionComponent.Origin, tileEndPoint)) {
                return;
            }

            // Render the tile map to the buffer, culling individual cells that are not in view of the camera
            for (int y = 0; y < tileMapComponent.width; y++) {
                for (int x = 0; x < tileMapComponent.height; x++) {
                    // check if not level - for debugging breakpoint
                    if (!Objects.equals(tileMapComponent.assetId, "level")) {
                        IO.println("Debug:  Rendering tile map at layer " + layerID + " with asset ID " + tileMapComponent.assetId);
//                        debugPrintTileMap(tileMapComponent, tileMap);
                    }
                    // Check bounds of tile map asset
                    if (y >= tileMap.length || x >= tileMap[y].length) {
                        continue;
                    }

                    Cell cell = tileMap[y][x];
                    if (cell == null) {
                        continue;
                    }
                    Point cellPosition = new Point(positionComponent.Origin.x() + x, positionComponent.Origin.y() + y);
                    if (!renderObjects.camera().isInView(cellPosition)) {
                        continue;
                    }

                    // Skip empty cells -- Allows transparency
                    if (cell.content == ' ') {
                        continue;
                    }


                    // Make cells relative to the camera position
                    Point relativePoint = renderObjects.camera().worldToScreen(cellPosition);
                    try {
                        Cell existingCell = buffer.cells[relativePoint.y()][relativePoint.x()];

                        if (existingCell != null && existingCell.content != ' ') {
                            continue;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        IO.println("Error:  Cell at " + relativePoint + " is out of bounds for the render buffer. Skipping this cell.");
                        continue;
                    }
                    buffer.cells[relativePoint.y()][relativePoint.x()] = cell;
                }
            }
        });
    }

    public static void debugPrintTileMap(TileMapComponent tileMapComponent, Cell[][] tileMapAsset) {
        IO.println("Debug: Printing tile map for layer " + tileMapComponent.assetId);
        for (int y = 0; y < tileMapComponent.width; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < tileMapComponent.height; x++) {
                if (y >= tileMapAsset.length || x >= tileMapAsset[y].length) {
                    row.append(' ');
                    continue;
                }
                Cell cell = tileMapAsset[y][x];
                if (cell == null) {
                    row.append(' ');
                } else {
                    row.append(cell.content);
                }
            }
            IO.println(row.toString());
        }
    }
}
