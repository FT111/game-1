package resources;

import com.fasterxml.jackson.databind.node.POJONode;
import engine.Resources;
import engine.Utils;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.OrientationComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import resources.components.VisionBlockerComponent;
import resources.components.VisionEmitterComponent;

import java.util.HashMap;
import java.util.HashSet;

public class VisionSystem extends System {
    protected int globalTickFrequency;
    private HashSet<Point> staticVisionBlockingPoints = new HashSet<>();
    private World world;
    private Resources resources;

    public VisionSystem(World world, Resources resources, HashMap<Point, HashSet<EntityID>> chunkMap, int globalTickFrequency) {
        this.world = world;
        this.resources = resources;
        this.globalTickFrequency = globalTickFrequency;
        bakeStaticVisionMap(world, resources);
    }

    public VisionSystem(World world, Resources resources) {
        this.world = world;
        this.resources = resources;
        this.globalTickFrequency = 1;
        bakeStaticVisionMap(world, resources);
    }

    private void bakeStaticVisionMap(World world, Resources resources) {
        world.ComponentLayersIndex.query(new Class[]{VisionBlockerComponent.class, TileMapComponent.class, PositionComponent.class}).forEach(layerID -> {
            var visionBlocker = (VisionBlockerComponent) world.Layers.get(layerID).get(VisionBlockerComponent.class);
            var tileMap = (TileMapComponent) world.Layers.get(layerID).get(TileMapComponent.class);
            var position = (PositionComponent) world.Layers.get(layerID).get(PositionComponent.class);

            Cell[][] tileMapAsset = resources.getAsset(tileMap.resourceId, tileMap.assetId, Cell[][].class);
            staticVisionBlockingPoints.addAll(Utils.extractTilePointsFromTileMap(tileMap, tileMapAsset, visionBlocker.blockingTiles, position));
            });
        }

    private HashSet<Point> calculateVisionBlockingPointsForEmitter(Point emitter, int range, OrientationComponent orientation) {
        // Recursively shadowcast outwards from a given emitter using an arc
        HashSet<Point> visionBlockingPoints = new HashSet<>();
        int arcAngle = 5; // smaller angle = more points = more accurate vision blocking but more expensive to calculate
        int startAngle = orientation.facingAngle - 180 - arcAngle;
        int endAngle = orientation.facingAngle + 180 + arcAngle;

        for (int r = 1; r <= range; r++) {
            HashSet<Point> arcPoints = drawArc(emitter, r, startAngle, endAngle);
            for (Point point : arcPoints) {
                if (staticVisionBlockingPoints.contains(point) || visionBlockingPoints.contains(point)) {
                    visionBlockingPoints.add(point);
                }
            }
        }

        return visionBlockingPoints;
    }

    private HashSet<Point> drawArc(Point center, int radius, int startAngle, int endAngle) {
        HashSet<Point> arcPoints = new HashSet<>();

        for (int angle = startAngle; angle <= endAngle; angle++) {
            double radians = Math.toRadians(angle);
            int x = center.x() + (int) Math.round(radius * Math.cos(radians));
            int y = center.y() + (int) Math.round(radius * Math.sin(radians));
            arcPoints.add(new Point(x, y));
        }

        return arcPoints;
    }


    @Override
    public void update(World world, int tickCount) {
        HashSet<EntityID> trackedVisionEmitters = world.ComponentEntitiesIndex.query(new Class[]{VisionEmitterComponent.class, OrientationComponent.class, PositionComponent.class});

        // Use a shadowcaster to calculate the vision blocking tiles for each emitter

        trackedVisionEmitters.forEach(entityID -> {;

            var entity = world.Entities.get(entityID);
            var emitter = (VisionEmitterComponent) entity.get(VisionEmitterComponent.class);
            var outputLayerID = (LayerID) ((VisionEmitterComponent) entity.get(VisionEmitterComponent.class)).visionLayer;
            var visionTileMap = (TileMapComponent) world.Layers.get(outputLayerID).get(TileMapComponent.class);

            // only update vision for this emitter every visionTickFrequency ticks - for optimisation
            if (tickCount % emitter.visionTickFrequency != 0) {
                return;
            }

            var orientation = (OrientationComponent) entity.get(OrientationComponent.class);
            var position = (PositionComponent) entity.get(PositionComponent.class);

            HashSet<Point> visionBlockingPoints = calculateVisionBlockingPointsForEmitter(position.Origin, emitter.visionRange, orientation);
            // Update vision layer position
            var visionLayerPosition = (PositionComponent) world.Layers.get(outputLayerID).get(PositionComponent.class);
            visionLayerPosition.Origin = new Point(position.Origin.x() - visionTileMap.width / 2, position.Origin.y() - visionTileMap.height / 2);

            IO.println("Vision blocking points for emitter " + entityID + ": " + visionBlockingPoints);
                // Update the tilemap for this emitter's vision layer
                Cell[][] tileMapAsset = resources.getAsset(visionTileMap.resourceId, visionTileMap.assetId, Cell[][].class);
                for (int y = 0; y < visionTileMap.width; y++) {
                    for (int x = 0; x < visionTileMap.height; x++) {
                        Point currentPoint = new Point(position.Origin.x() + x, position.Origin.y() + y);
                        if (visionBlockingPoints.contains(currentPoint)) {
                            tileMapAsset[y][x] = new Cell('%'); // blocked
                        } else {
                            tileMapAsset[y][x] = new Cell(' '); // visible
                        }
                    }
                }

            });
    }
}
