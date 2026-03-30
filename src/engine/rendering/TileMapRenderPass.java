package engine.rendering;

import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.*;

import java.util.HashSet;
import java.util.Objects;

public class TileMapRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        HashSet<LayerID> tileMapEntities = (HashSet<LayerID>) renderObjects.world().ComponentLayersIndex.query(new Class[] {TileMapComponent.class, PositionComponent.class});

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

            // Cull tile map if it is not in view of the camera and a world-positioned object
            if (!positionComponent.isStatic && !renderObjects.camera().isInView(positionComponent.Origin, tileEndPoint)) {
                return;
            }

            // Render the tile map to the buffer, culling individual cells that are not in view of the camera
            for (int y = 0; y < tileMapComponent.width; y++) {
                for (int x = 0; x < tileMapComponent.height; x++) {
                    // Check bounds of tile map asset
                    if (y >= tileMap.length || x >= tileMap[y].length) {
                        continue;
                    }

                    Cell cell = tileMap[y][x];
                    if (cell == null) {
                        continue;
                    }

                    cell.zIndex = positionComponent.zIndex;
                    // convert from tilemap-relative coords to world coords (or screen coords if a static tilemap)
                    Point cellPosition = new Point(positionComponent.Origin.x() + x, positionComponent.Origin.y() + y);
                    // cull if world coords not in view of screen
                    if (!renderObjects.camera().isInView(cellPosition)) {
                        continue;
                    }

                    // convert from world coords to screen coords, if the tilemap isn't static (meaning it is already in screen coords)
                    if (!positionComponent.isStatic) {
                        cellPosition = renderObjects.camera().worldToScreen(cellPosition);
                    }
                    try {
                        Cell existingCell = buffer.cells[cellPosition.y()][cellPosition.x()];

                        cell = collateCells(existingCell, cell);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // IO.println("Error:  Cell at " + relativePoint + " is out of bounds for the render buffer. Skipping this cell.");
                        continue;
                    }
                    buffer.cells[cellPosition.y()][cellPosition.x()] = cell;
                }
            }
        });
    }

    // merges the properties, prioritising the non null content. If both are non null, prefer the cell with the higher z-index
    public static Cell collateCells(Cell existing, Cell added) {
        if (existing == null) {
            return added;
        }
        if (added == null) {
            return existing;
        }

        // Always prefer the non null content. If both are non null, prefer the cell with the higher z-index
        char content = collateCellProperty(existing.content, added.content, existing.zIndex, added.zIndex);
        Colour bgColour = collateCellProperty(existing.bgColour, added.bgColour, existing.zIndex, added.zIndex);
        Colour fgColour = collateCellProperty(existing.fgColour, added.fgColour, existing.zIndex, added.zIndex );

        return new Cell(content, fgColour, bgColour, Math.max(existing.zIndex, added.zIndex));
    }

    private static <T> T collateCellProperty(T existing, T added, int existingZ, int addedZ) {
        T outputProperty;
        if (existing != null && added != null) {
            outputProperty = (existingZ >= addedZ) ? existing : added;
        } else {
            outputProperty = (existing != null) ? existing : added;
        }
        return (T) outputProperty;
    }

    public static void debugPrintTileMap(TileMapComponent tileMapComponent, Cell[][] tileMapAsset) {
        // IO.println("Debug: Printing tile map for layer " + tileMapComponent.assetId);
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
            // IO.println(row.toString());
        }
    }
}
