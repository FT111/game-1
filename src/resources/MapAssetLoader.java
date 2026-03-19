package resources;

import engine_interfaces.objects.ResourceLoader;
import engine_interfaces.objects.rendering.Cell;

import java.util.HashMap;

public class MapAssetLoader implements ResourceLoader {
    private final Cell[][] testLevel = new Cell[][] {
            {new Cell('#'), new Cell('#'), new Cell('#'), new Cell('#'), new Cell('#')},
            {new Cell('#'), new Cell('.'), new Cell('.'), new Cell('.'), new Cell('#')},
            {new Cell('#'), new Cell('.'), new Cell('.'), new Cell
                    ('.'), new Cell('#')},
            {new Cell('#'), new Cell('.'), new Cell('.'), new Cell('.'), new Cell('#')},
            {new Cell('#'), new Cell('#'), new Cell('#'), new Cell('#'), new Cell('#')}
    };


    @Override
    public String getKey() {
        return "mapAssets";
    }

    @Override
    public HashMap<String, Object> Load() {
        return new HashMap<>() {{
            put("level", testLevel);
        }};
    }

    @Override
    public void Save() {

    }

    @Override
    public boolean isWritable() {
        return false;
    }
}
