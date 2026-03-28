package resources;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.ResourceLoader;
import engine_interfaces.objects.rendering.Cell;

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
        HashSet<EntityID> emittingEntities = (HashSet<EntityID>) world.ComponentEntitiesIndex.query(resources.components.VisionEmitterComponent.class);

        emittingEntities.forEach(entityId -> {
            blankVisionTileMaps.put(entityId.toString(), new Cell[100][100]);
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
