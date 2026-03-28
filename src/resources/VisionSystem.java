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
    private static final Point EMITTER_ORIGIN = new Point(0, 0);
    private static final int[][] OCTANT_TRANSFORMS = new int[][]{
            {1, 0, 0, -1, -1, 0, 0, 1},
            {0, 1, -1, 0, 0, -1, 1, 0},
            {0, 1, 1, 0, 0, -1, -1, 0},
            {1, 0, 0, 1, -1, 0, 0, -1}
    };

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

    private HashSet<Point> buildRelativeVisionBlockMap(Point emitterPos, int maxRange) {
        var relativeBlockers = new HashSet<Point>();
        staticVisionBlockMap.forEach(blocker -> {
            Point relative = blocker.subtract(emitterPos);
            if (Math.max(Math.abs(relative.x()), Math.abs(relative.y())) <= maxRange) {
                relativeBlockers.add(relative);
            }
        });
        return relativeBlockers;
    }


    private static HashSet<Point> castView(Point emitterPos, int maxRange, HashSet<Point> visionBlockMap) {
        var visiblePoints = new HashSet<Point>();
        visiblePoints.add(emitterPos);

        for (int octant = 0; octant < 8; octant++) {
            castLight(visiblePoints, visionBlockMap, emitterPos.x(), emitterPos.y(), 1, 1.0, 0.0, maxRange,
                    OCTANT_TRANSFORMS[0][octant], OCTANT_TRANSFORMS[1][octant],
                    OCTANT_TRANSFORMS[2][octant], OCTANT_TRANSFORMS[3][octant]);
        }

        return visiblePoints;
    }

    private static void castLight(HashSet<Point> visiblePoints, HashSet<Point> visionBlockMap,
                                  int originX, int originY, int row, double startSlope, double endSlope,
                                  int radius, int xx, int xy, int yx, int yy) {
        if (startSlope < endSlope) {
            return;
        }

        int radiusSquared = radius * radius;

        for (int depth = row; depth <= radius; depth++) {
            int dx = -depth - 1;
            int dy = -depth;
            boolean blocked = false;
            double newStartSlope = 0.0;

            while (dx <= 0) {
                dx += 1;

                int translatedX = originX + dx * xx + dy * xy;
                int translatedY = originY + dx * yx + dy * yy;
                double leftSlope = (dx - 0.5) / (dy + 0.5);
                double rightSlope = (dx + 0.5) / (dy - 0.5);

                if (startSlope < rightSlope) {
                    continue;
                }
                if (endSlope > leftSlope) {
                    break;
                }

                Point currentPoint = new Point(translatedX, translatedY);
                int distanceSquared = (translatedX - originX) * (translatedX - originX)
                        + (translatedY - originY) * (translatedY - originY);

                boolean blocksVision = visionBlockMap.contains(currentPoint);
                if (distanceSquared <= radiusSquared && !blocksVision) {
                    visiblePoints.add(currentPoint);
                }


                if (blocked) {
                    if (blocksVision) {
                        newStartSlope = rightSlope;
                        continue;
                    }

                    blocked = false;
                    startSlope = newStartSlope;
                } else if (blocksVision && depth < radius) {
                    blocked = true;
                    castLight(visiblePoints, visionBlockMap, originX, originY, depth + 1, startSlope, leftSlope,
                            radius, xx, xy, yx, yy);
                    newStartSlope = rightSlope;
                }
            }

            if (blocked) {
                break;
            }
        }
    }

    private static HashSet<Point> filterPointsWithinFov(HashSet<Point> points, OrientationComponent orientation, int fieldOfViewAngle) {
        var filteredPoints = new HashSet<Point>();
        double facingRadians = Math.toRadians(orientation.facingAngle);
        double halfFov = Math.toRadians(Math.max(0, Math.min(fieldOfViewAngle, 360))) / 2.0;

        points.forEach(point -> {
            if (point.x() == 0 && point.y() == 0) {
                filteredPoints.add(point);
                return;
            }
            double angle = Math.atan2(point.y(), point.x());
            double delta = normaliseAngle(angle - facingRadians);
            if (Math.abs(delta) <= halfFov) {
                filteredPoints.add(point);
            }
        });

        return filteredPoints;
    }

    private static double normaliseAngle(double angle) {
        double twoPi = Math.PI * 2;
        while (angle <= -Math.PI) {
            angle += twoPi;
        }
        while (angle > Math.PI) {
            angle -= twoPi;
        }
        return angle;
    }

    public HashSet<Point> calculatePointsInLineOfSight(Point emitterPos, OrientationComponent orientation, int fieldOfViewAngle, int range) {
        var relativeVisionBlocks = buildRelativeVisionBlockMap(emitterPos, range);
        var allVisiblePoints = castView(EMITTER_ORIGIN, range, relativeVisionBlocks);
        return filterPointsWithinFov(allVisiblePoints, orientation, fieldOfViewAngle);
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

//            // only update vision for this emitter every visionTickFrequency ticks - for optimisation
//            if (tickCount % emitter.visionTickFrequency != 0) {
//                return;
//            }

            var orientation = (OrientationComponent) entity.get(OrientationComponent.class);
            var position = (PositionComponent) entity.get(PositionComponent.class);

            // Update vision layer position
            var visionLayerPosition = (PositionComponent) world.Layers.get(outputLayerID).get(PositionComponent.class);

            var pointsInSight = calculatePointsInLineOfSight(position.Origin, orientation, emitter.fieldOfViewAngle, emitter.visionRange);
            // IO.println(pointsInSight);

            // Create a new tile map asset for the vision layer based on the points in sight, and update the vision layer's tile map asset with it
            Cell[][] visionTileMapAsset = new Cell[visionTileMap.height][visionTileMap.width];
            for (Point point : pointsInSight) {
                int x = point.x() + visionTileMap.width / 2;
                int y = point.y() + visionTileMap.height /2;
                if (y >= 0 && y < visionTileMap.height && x >= 0 && x < visionTileMap.width) {
                    visionTileMapAsset[y][x] = new Cell('+');
                }
            }

            // Update the vision layer tile map asset with the new vision tile map
            resources.setAsset(visionTileMap.resourceId, visionTileMap.assetId, visionTileMapAsset);

            // Make the top center of the asset appear as the origin point of the vision layer, and thus the emitter
            var newVisionOutputLayerPosition = new PositionComponent(new Point(position.Origin.x() - visionTileMap.width / 2,
                    position.Origin.y() - visionTileMap.height /2));
            world.Layers.get(outputLayerID).put(PositionComponent.class, newVisionOutputLayerPosition);

            // IO.println("Updated vision layer position to " + ((PositionComponent) world.Layers.get(outputLayerID).get(PositionComponent.class)).Origin);
            });
    }
}
