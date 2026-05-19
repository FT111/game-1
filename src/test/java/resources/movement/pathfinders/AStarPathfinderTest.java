package resources.movement.pathfinders;

import engine.layout.SpatialMap;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AStarPathfinderTest {

    private SpatialMap buildArena(int minX, int minY, int maxX, int maxY) {
        SpatialMap collision = new SpatialMap("collision");
        HashSet<Point> walls = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            walls.add(new Point(x, minY));
            walls.add(new Point(x, maxY));
        }
        for (int y = minY; y <= maxY; y++) {
            walls.add(new Point(minX, y));
            walls.add(new Point(maxX, y));
        }
        collision.addLayer(new LayerID("walls"), walls);
        return collision;
    }

    @Test
    void findPathAvoidsBlockedPoints() {
        SpatialMap collision = buildArena(0, 0, 4, 4);
        HashSet<Point> blocked = new HashSet<>();
        blocked.add(new Point(2, 1));
        collision.addLayer(new LayerID("obstacles"), blocked);

        AStarPathfinder pathfinder = new AStarPathfinder();
        List<Point> path = pathfinder.findPath(new Point(1, 1), new Point(3, 1), collision);

        assertFalse(path.isEmpty());
        assertTrue(path.contains(new Point(1, 1)));
        assertTrue(path.contains(new Point(3, 1)));
        assertFalse(path.contains(new Point(2, 1)));
    }

    @Test
    void findPathReturnsEmptyWhenBlocked() {
        SpatialMap collision = buildArena(0, 0, 4, 4);
        HashSet<Point> blocked = new HashSet<>();
        blocked.add(new Point(2, 1));
        blocked.add(new Point(2, 2));
        blocked.add(new Point(2, 3));
        collision.addLayer(new LayerID("obstacles"), blocked);

        AStarPathfinder pathfinder = new AStarPathfinder();
        List<Point> path = pathfinder.findPath(new Point(1, 2), new Point(3, 2), collision);

        assertTrue(path.isEmpty());
    }
}
