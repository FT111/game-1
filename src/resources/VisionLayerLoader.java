package resources;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.ResourceLoader;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import resources.components.VisionLayerComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class VisionLayerLoader implements ResourceLoader {
    private HashMap<String, Object> blankVisionTileMaps = new HashMap<>();

    public VisionLayerLoader(World world) {
        // Get emitting entities
        HashSet<LayerID> visionLayers = world.ComponentLayersIndex.query(
                VisionLayerComponent.class,
                TileMapComponent.class,
                DimensionsComponent.class
        );

        visionLayers.forEach(layerId -> {
            var emitterId = world.getLayerComponent(layerId, VisionLayerComponent.class).emitter;
            var tileMap = world.getLayerComponent(layerId, TileMapComponent.class);
            var dimensions = world.getLayerComponent(layerId, DimensionsComponent.class);

            blankVisionTileMaps.put(emitterId.toString(), new Cell[dimensions.height][dimensions.width]);
        });

    }


    @Override
    public String getKey() {
        return "vision-maps";
    }

    @Override
    public HashMap<String, Object> Load() {
        return blankVisionTileMaps;
    }

    @Override
    public void Save() {

    }

    @Override
    public boolean isWritable() {
        return false;
    }

}
