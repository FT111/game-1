package tests.engine.rendering;

import engine.EventBus;
import engine.Resources;
import engine.World;
import engine.layout.LayoutManager;
import engine.rendering.RenderPassUtils;
import engine_interfaces.objects.CameraView;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.renderObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import engine_interfaces.objects.components.MaskToTileMapComponent;
import tests.testsupport.TestResourceLoader;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenderPassUtilsTest {

    @Test
    void isLayerInViewRespectsFixedPositioning() {
        CameraView camera = new CameraView(0, 0, 5, 5);
        renderObjects renderObjects = new renderObjects(null, null, camera, null);
        PositionComponent fixed = new PositionComponent(new Point(10, 10));
        fixed.positionStrategy = Positioning.FIXED;

        Assertions.assertTrue(RenderPassUtils.isLayerInView(renderObjects, fixed, null));
    }

    @Test
    void isLayerInViewChecksWorldBounds() {
        CameraView camera = new CameraView(0, 0, 5, 5);
        renderObjects renderObjects = new renderObjects(null, null, camera, null);
        PositionComponent pos = new PositionComponent(new Point(10, 10));

        assertFalse(RenderPassUtils.isLayerInView(renderObjects, pos, new engine_interfaces.objects.components.DimensionsComponent(2, 2)));
    }

    @Test
    void visionMaskVisibilityChecksTileMap() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        Resources resources = new Resources();
        LayoutManager layoutManager = new LayoutManager(world, bus, resources);

        Cell[][] map = new Cell[][]{
                {new Cell('#'), null},
                {null, null}
        };
        HashMap<String, Object> assets = new HashMap<>();
        assets.put("vision", map);
        resources.addResourceLoader(new TestResourceLoader("tiles", assets, false));

        LayerID visionLayer = world.createLayer(
                new PositionComponent(new Point(0, 0)),
                new TileMapComponent("tiles", "vision", "origin", false)
        );

        renderObjects renderObjects = new renderObjects(world, resources, new CameraView(0, 0, 5, 5), layoutManager);
        MaskToTileMapComponent mask = new MaskToTileMapComponent(visionLayer);

        assertTrue(RenderPassUtils.isPointVisibleInVisionMask(renderObjects, mask, 0, 0));
        assertFalse(RenderPassUtils.isPointVisibleInVisionMask(renderObjects, mask, 1, 0));
        assertTrue(RenderPassUtils.isAnyPointVisibleInVisionMask(renderObjects, mask, List.of(new Point(1, 0), new Point(0, 0))));
    }

    @Test
    void trySetCellCollatesIntoBuffer() {
        RenderBuffer buffer = new RenderBuffer(1, 1);
        Cell existing = new Cell('a', 1);
        Cell added = new Cell('b', 2);
        buffer.cells[0][0] = existing;

        RenderPassUtils.trySetCell(buffer, 0, 0, added);
        RenderPassUtils.trySetCell(buffer, 2, 2, added);

        assertEquals('b', buffer.cells[0][0].content);
        assertEquals(2, buffer.cells[0][0].zIndex);
    }
}
