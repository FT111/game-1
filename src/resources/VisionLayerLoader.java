package resources;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.ResourceLoader;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import resources.components.VisionLayerComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class VisionLayerLoader implements ResourceLoader {
    private HashMap<String, Object> blankVisionTileMaps = new HashMap<>();
    private Cell[][] debugFilledTileMap = new Cell[50][50];

    public VisionLayerLoader(World world) {
        for (Cell[] cells : debugFilledTileMap) {
            for (Cell cell : cells) {
                if (cell == null) { cell = new Cell('&');}
            }
        }

        // Get emitting entities
        HashSet<LayerID> visionLayers = (HashSet<LayerID>) world.ComponentLayersIndex.query(new Class[]{VisionLayerComponent.class, TileMapComponent.class});

        visionLayers.forEach(layerId -> {
            var layer = world.Layers.get(layerId);
            var emitterId =((VisionLayerComponent) layer.get(VisionLayerComponent.class)).emitter;
            var tileMap = (TileMapComponent) layer.get(TileMapComponent.class);

            blankVisionTileMaps.put(emitterId.toString(), new Cell[tileMap.height][tileMap.width]);
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
