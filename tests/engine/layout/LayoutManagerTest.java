package tests.engine.layout;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine.layout.LayoutManager;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import org.junit.jupiter.api.Test;
import game.components.VisionBlockerComponent;
import tests.testsupport.TestResourceLoader;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LayoutManagerTest {

    @Test
    void fromBoundingBoxRegistersPointsOnLayerCreation() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        Resources resources = new Resources();
        LayoutManager layoutManager = new LayoutManager(world, bus, resources);

        layoutManager.defineMapLayer("bounds")
                .fromBoundingBox(VisionBlockerComponent.class)
                .register();

        var layerId = world.createLayer(
                new VisionBlockerComponent(new HashSet<>()),
                new PositionComponent(new Point(3, 4)),
                new DimensionsComponent(2, 3)
        );

        bus.flush();

        var points = layoutManager.getSpatialMap("bounds").getPointsForLayer(layerId);
        assertEquals(6, points.size());
        assertTrue(points.contains(new Point(3, 4)));
        assertTrue(points.contains(new Point(4, 6)));
    }

    @Test
    void fromTileMapExtractsMatchingTiles() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        Resources resources = new Resources();
        LayoutManager layoutManager = new LayoutManager(world, bus, resources);

        Cell[][] map = new Cell[][]{
                {new Cell('#'), new Cell('.')},
                {new Cell('.'), new Cell('#')}
        };
        HashMap<String, Object> assets = new HashMap<>();
        assets.put("map", map);
        resources.addResourceLoader(new TestResourceLoader("tiles", assets, false));

        layoutManager.defineMapLayer("collision")
                .fromTileMap(VisionBlockerComponent.class, c -> c.blockingTiles)
                .register();

        HashSet<Character> blocking = new HashSet<>();
        blocking.add('#');

        var layerId = world.createLayer(
                new VisionBlockerComponent(blocking),
                new PositionComponent(new Point(1, 1)),
                new DimensionsComponent(2, 2),
                new TileMapComponent("tiles", "map", "origin", false)
        );

        bus.flush();

        var points = layoutManager.getSpatialMap("collision").getPointsForLayer(layerId);
        assertEquals(2, points.size());
        assertTrue(points.contains(new Point(1, 1)));
        assertTrue(points.contains(new Point(2, 2)));
    }

    @Test
    void fromTileMapWithNullTilesExtractsAllNonNullCells() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        Resources resources = new Resources();
        LayoutManager layoutManager = new LayoutManager(world, bus, resources);

        Cell[][] map = new Cell[][]{
                {new Cell('#'), null},
                {new Cell(null), new Cell('.')}
        };
        HashMap<String, Object> assets = new HashMap<>();
        assets.put("map", map);
        resources.addResourceLoader(new TestResourceLoader("tiles", assets, false));

        layoutManager.defineMapLayer("all-tiles")
                .fromTileMap(VisionBlockerComponent.class, c -> c.blockingTiles)
                .register();

        var layerId = world.createLayer(
                new VisionBlockerComponent(null),
                new PositionComponent(new Point(2, 3)),
                new DimensionsComponent(2, 2),
                new TileMapComponent("tiles", "map", "origin", false)
        );

        bus.flush();

        var points = layoutManager.getSpatialMap("all-tiles").getPointsForLayer(layerId);
        assertEquals(2, points.size());
        assertTrue(points.contains(new Point(2, 3)));
        assertTrue(points.contains(new Point(3, 4)));
    }
}
