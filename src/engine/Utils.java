package engine;

import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.Colour;

import java.util.HashSet;

public class Utils {
    public static HashSet<Point> extractTilePointsFromTileMap(TileMapComponent tileMapComponent, DimensionsComponent dimensionsComponent, Cell[][] tileMapAsset, HashSet<Character> extractedTiles, PositionComponent positionDetails) {
        HashSet<Point> extractedPoints = new HashSet<>();

        for (int y = 0; y < dimensionsComponent.width; y++) {
            for (int x = 0; x < dimensionsComponent.height; x++) {
                if (y >= tileMapAsset.length || x >= tileMapAsset[y].length) {
                    continue;
                }

                Cell cell = tileMapAsset[y][x];
                if (cell == null) {
                    continue;
                }
                if (extractedTiles.contains(cell.content)) {
                    Point collisionPoint = new Point(positionDetails.Origin.x() + x, positionDetails.Origin.y() + y);
                    extractedPoints.add(collisionPoint);
                }
            }
        }
        return extractedPoints;
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
        Colour fgColour = collateCellProperty(existing.fgColour, added.fgColour, existing.zIndex, added.zIndex);

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
}
