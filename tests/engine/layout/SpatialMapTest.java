package tests.engine.layout;

import engine.layout.SpatialMap;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpatialMapTest {

    @Test
    void addAndRemoveLayerUpdatesIndexes() {
        SpatialMap map = new SpatialMap("test");
        LayerID layerId = new LayerID("layer-1");
        HashSet<Point> points = new HashSet<>();
        points.add(new Point(1, 2));
        points.add(new Point(2, 2));

        map.addLayer(layerId, points);

        assertEquals(2, map.getPointsForLayer(layerId).size());
        assertTrue(map.getLayersAt(new Point(1, 2)).contains(layerId));

        map.removeLayer(layerId);

        assertTrue(map.getPointsForLayer(layerId).isEmpty());
        assertTrue(map.getLayersAt(new Point(1, 2)).isEmpty());
    }
}
