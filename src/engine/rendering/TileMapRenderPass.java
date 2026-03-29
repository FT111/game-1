package engine.rendering;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.*;

import java.util.Arrays;
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

            // Cull tile map if it is not in view of the camera
            if (!renderObjects.camera().isInView(positionComponent.Origin, tileEndPoint)) {
                return;
            }
            if (!Objects.equals(tileMapComponent.assetId, "level")) {
//                // IO.println("Debug:  Rendering tile map at layer " + layerID + " with asset ID " + tileMapComponent.assetId);
//                debugPrintTileMap(tileMapComponent, tileMap);
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
                    Point cellPosition = new Point(positionComponent.Origin.x() + x, positionComponent.Origin.y() + y);
                    if (!renderObjects.camera().isInView(cellPosition)) {
                        continue;
                    }


                    // Make cells relative to the camera position
                    Point relativePoint = renderObjects.camera().worldToScreen(cellPosition);
                    try {
                        Cell existingCell = buffer.cells[relativePoint.y()][relativePoint.x()];

                        cell = collateCells(existingCell, cell);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // IO.println("Error:  Cell at " + relativePoint + " is out of bounds for the render buffer. Skipping this cell.");
                        continue;
                    }
                    buffer.cells[relativePoint.y()][relativePoint.x()] = cell;
                }
            }
        });
    }

    // merges the properties, prioritising the existing cell
    public static Cell collateCells(Cell existing, Cell added) {
        if (existing == null) {
            return added;
        }
        if (added == null) {
            return existing;
        }

        // Always prefer the non null content. If both are non null, prefer the existing cell's content (only if not a space or '.'). If both null, use a space.
        // If the existing is a . and the new is null, prefer the .
        Character content;
        if (existing.content != null && existing.content != ' ' && existing.content != '.') {
            content = existing.content;
        } else if (added.content != null && added.content != ' ' && added.content != '.') {
            content = added.content;
        } else if (existing.content != null) {
            content = existing.content;
        }
        else {
            content = ' ';
        }


        Colour fgColour = (added.fgColour == null) ? existing.fgColour : added.fgColour;
        Colour bgColour = (added.bgColour == null) ? existing.bgColour : added.bgColour;

        return new Cell(content, fgColour, bgColour);
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
