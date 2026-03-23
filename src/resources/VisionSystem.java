package resources;

import engine.Resources;
import engine.Utils;
import engine.World;
import engine_interfaces.objects.EntityID;
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



    @Override
    public void update(World world, int tickCount) {
        HashSet<EntityID> trackedVisionEmitters = world.ComponentEntitiesIndex.query(new Class[]{VisionEmitterComponent.class, OrientationComponent.class, PositionComponent.class});

        // Use an octet shadowcaster to calculate the vision blocking tiles for each emitter

        trackedVisionEmitters.forEach(entityID -> {;
            var entity = world.Entities.get(entityID);
            var emitter = (VisionEmitterComponent) entity.get(VisionEmitterComponent.class);
            // only update vision for this emitter every visionTickFrequency ticks - for optimisation
            if (tickCount % emitter.visionTickFrequency != 0) {
                return;
            }

            var orientation = (OrientationComponent) entity.get(OrientationComponent.class);
            var position = (PositionComponent) entity.get(PositionComponent.class);


        });
    }
}
