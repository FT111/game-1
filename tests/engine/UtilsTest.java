package tests.engine;

import engine.Utils;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.Colour;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UtilsTest {

    @Test
    void extractTilePointsFromTileMapRespectsOffsetsAndTiles() {
        Cell[][] tileMap = new Cell[][]{
                {new Cell('#'), new Cell('.')},
                {null, new Cell('#')}
        };
        var dimensions = new DimensionsComponent(2, 2);
        var position = new PositionComponent(new Point(5, 10));
        var tiles = new HashSet<Character>();
        tiles.add('#');

        var points = Utils.extractTilePointsFromTileMap(new TileMapComponent("res", "asset", "origin", false), dimensions, tileMap, tiles, position);

        assertEquals(2, points.size());
        assertEquals(true, points.contains(new Point(5, 10)));
        assertEquals(true, points.contains(new Point(6, 11)));
    }

    @Test
    void collateCellsPrefersHigherZIndex() {
        Cell existing = new Cell('a', new Colour(1, 2, 3), new Colour(4, 5, 6), 1);
        Cell added = new Cell('b', new Colour(7, 8, 9), new Colour(10, 11, 12), 3);

        Cell result = Utils.collateCells(existing, added);

        assertEquals('b', result.content);
        assertEquals(3, result.zIndex);
        assertEquals(7, result.fgColour.R);
        assertEquals(10, result.bgColour.R);
    }

    @Test
    void collateCellsHandlesNulls() {
        Cell added = new Cell('x');

        Cell result = Utils.collateCells(null, added);

        assertEquals('x', result.content);
        assertNull(result.fgColour);
        assertNull(result.bgColour);
    }
}
