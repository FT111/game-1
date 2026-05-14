package resources.movement;

import engine.layout.SpatialMap;
import engine_interfaces.objects.Point;

import java.util.List;

public interface PathfindingStrategy {
    public List<Point> findPath(Point start, Point end, SpatialMap collisionMap);
}
