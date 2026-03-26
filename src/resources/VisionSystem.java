package resources;

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
    private HashSet<Point> staticVisionBlockMap = new HashSet<>();
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
            staticVisionBlockMap.addAll(Utils.extractTilePointsFromTileMap(tileMap, tileMapAsset, visionBlocker.blockingTiles, position));
            });
        }


    private static HashSet<Point> castView(Point emitterPos, double startGradient, double endGradient, int maxRange, HashSet<Point> visionBlockMap) {
        // Recursive shadow caster, divides into 8 octants and casts shadows for each octant separately, then combines results
        var visiblePoints = new HashSet<Point>();

        for (int octant = 0; octant < 8; octant++) {
            visiblePoints.addAll(castViewInOctant(emitterPos, startGradient, endGradient, maxRange, visionBlockMap,  octant));
        }

        return visiblePoints;
    }

    private static HashSet<Point> castViewInOctant(Point emitterPos, double startGradient, double endGradient, int maxRange, HashSet<Point> visionBlockMap, int octant) {
        // Casts view in a single octant, recursively dividing into smaller sections
        // Returns a set of points that are visible in this octant
        var visiblePoints = new HashSet<Point>();

        for (int depth = 1; depth <= maxRange; depth++) {
            // for each point at this depth, calculate the gradient and check if it is within the start and end gradients for this section
            for (int i = 0; i <= depth; i++) {
                Point point = getPointInOctant(emitterPos, i, depth, octant);
                double gradient = calculateGradient(emitterPos, point);

                if (gradient < startGradient || gradient > endGradient) {
                    continue;
                }

                visiblePoints.add(point);

                if (visionBlockMap.contains(point)) {
                    // If this point is a vision blocker, we need to cast a shadow behind it by recursively calling this function with updated gradients
                    double newStartGradient = calculateGradient(emitterPos, getPointInOctant(emitterPos, i - 1, depth, octant));
                    double newEndGradient = calculateGradient(emitterPos, getPointInOctant(emitterPos, i + 1, depth, octant));

                    visiblePoints.addAll(castViewInOctant(emitterPos, newStartGradient, newEndGradient, maxRange - depth, visionBlockMap, octant));
                }
            }
        }

        return visiblePoints;
    }

    private static Point getPointInOctant(Point emitterPos, int i, int depth, int octant) {
        // Returns the point at the given depth and index in the given octant
        switch (octant) {
            case 0:
                return new Point(emitterPos.x() + i, emitterPos.y() - depth);
            case 1:
                return new Point(emitterPos.x() + depth, emitterPos.y() - i);
            case 2:
                return new Point(emitterPos.x() + depth, emitterPos.y() + i);
            case 3:
                return new Point(emitterPos.x() + i, emitterPos.y() + depth);
            case 4:
                return new Point(emitterPos.x() - i, emitterPos.y() + depth);
            case 5:
                return new Point(emitterPos.x() - depth, emitterPos.y() + i);
            case 6:
                return new Point(emitterPos.x() - depth, emitterPos.y() - i);
            case 7:
                return new Point(emitterPos.x() - i, emitterPos.y() - depth);
            default:
                throw new IllegalArgumentException("Invalid octant: " + octant);
        }
    }

    private static double calculateGradient(Point emitterPos, Point point) {
        return Math.atan2(point.y() - emitterPos.y(), point.x() - emitterPos.x());
    }

    public HashSet<Point> calculatePointsInLineOfSight(Point emitterPos, OrientationComponent orientation, int range) {
        // Calculate the points in line of sight for a given emitter position, orientation and range, using the shadowcaster and the static vision block map
        double startGradient = Math.toRadians(orientation.facingAngle - 45);
        double endGradient = Math.toRadians(orientation.facingAngle + 45);

        return castView(emitterPos, startGradient, endGradient, range, staticVisionBlockMap);
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

            // Update vision layer position
            var visionLayerPosition = (PositionComponent) world.Layers.get(outputLayerID).get(PositionComponent.class);
            visionLayerPosition.Origin = new Point(position.Origin.x() - visionTileMap.width / 2, position.Origin.y() - visionTileMap.height / 2);

            var pointsInSight = calculatePointsInLineOfSight(position.Origin, orientation, emitter.visionRange);
            IO.println(pointsInSight);

            // Create a new tile map with the same dimensions as the vision layer, with cells that are visible in sight set to a specific character (e.g. 'V') and cells that are not visible set to null
            Cell[][] visionTileMapAsset = new Cell[visionTileMap.height][visionTileMap.width];
            for (int y = 0; y < visionTileMap.height; y++) {
                for (int x = 0; x < visionTileMap.width; x++) {
                    Point cellPoint = new Point(visionLayerPosition.Origin.x() + x, visionLayerPosition.Origin.y() + y);
                    if (pointsInSight.contains(cellPoint)) {
                        visionTileMapAsset[y][x] = new Cell('V');
                    } else {
                        visionTileMapAsset[y][x] = null;
                    }
                }
            }

                // Update the vision layer tile map asset with the new vision tile map
                resources.setAsset(visionTileMap.resourceId, visionTileMap.assetId, visionTileMapAsset);
            });
    }
}
