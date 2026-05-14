package engine.layout;

import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SpatialMap {
    private final String name;
    private final HashMap<LayerID, HashSet<Point>> byLayer = new HashMap<>(); // local cache
    private final HashMap<Point, HashSet<LayerID>> byPoint = new HashMap<>(); // query index

    public SpatialMap(String name) {
        this.name = name;
    }

    public void addLayer(LayerID layer, HashSet<Point> points) {
        byLayer.put(layer, points);
        for (Point p : points) {
            byPoint.computeIfAbsent(p, k -> new HashSet<>()).add(layer);
        }
    }

    public void removeLayer(LayerID layer) {
        HashSet<Point> points = byLayer.remove(layer);
        if (points != null) {
            for (Point p : points) {
                HashSet<LayerID> layers = byPoint.get(p);
                if (layers != null) {
                    layers.remove(layer);
                    if (layers.isEmpty()) {
                        byPoint.remove(p);
                    }
                }
            }
        }
    }

    public Set<LayerID> getLayersAt(Point p) {
        return byPoint.getOrDefault(p, new HashSet<>());
    }

    public Set<Point> getPointsForLayer(LayerID layer) {
        return byLayer.getOrDefault(layer, new HashSet<>());
    }
}
