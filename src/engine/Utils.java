package engine;

import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;

import java.util.HashSet;

public class Utils {
    public static HashSet<Point> extractTilePointsFromTileMap(TileMapComponent tileMapComponent, Cell[][] tileMapAsset, HashSet<Character> extractedTiles, PositionComponent positionDetails) {
        HashSet<Point> extractedPoints = new HashSet<>();

        for (int y = 0; y < tileMapComponent.width; y++) {
            for (int x = 0; x < tileMapComponent.height; x++) {
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
}
