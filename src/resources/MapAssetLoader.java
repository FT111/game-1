package resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine_interfaces.objects.rendering.Cell;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapAssetLoader implements engine_interfaces.objects.ResourceLoader {
    public static class JsonMapDefinition {
        public String name;
        public Map<String, String> legend;
        public List<String> rows;
    }

    private static final Set<Character> allowedTiles = Set.of(
            '#', '.', 'D', 'G', 'R', 'd', 'c', 'f', 'x', 'p', 'k', 'b', 'E', 'S', 't', 's', ' ','=',':', '+','*'
    );

    public Cell[][] loadMapFromJsonResource(String resourcePath) {
        try (InputStream is = MapAssetLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Map resource not found on classpath: " + resourcePath);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonMapDefinition def = mapper.readValue(is, JsonMapDefinition.class);

            if (def.rows == null || def.rows.isEmpty()) {
                throw new IllegalArgumentException("Map has no rows: " + resourcePath);
            }

            int height = def.rows.size();
            int width = def.rows.stream().mapToInt(String::length).max().orElse(0);
            Cell[][] map = new Cell[height][width];

            for (int y = 0; y < height; y++) {
                String row = def.rows.get(y);

                for (int x = 0; x < width; x++) {
                    char tile = (x < row.length()) ? row.charAt(x) : ' ';
                    if (!allowedTiles.contains(tile)) {
                        tile = '.'; // fallback for unknown chars
                    }

                    map[y][x] = new Cell(tile, null, null);
                }
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load map JSON from resource: " + resourcePath, e);
        }
    }

    @Override
    public String getKey() {
        return "mapAssets";
    }

    @Override
    public HashMap<String, Object> Load() {
        Cell[][] office80s = loadMapFromJsonResource("maps/office_level_80s.json");
        return new HashMap<>() {{
            put("level", office80s);
            put("office_80s", office80s);
        }};
    }

    @Override
    public void Save() { }

    @Override
    public boolean isWritable() {
        return false;
    }
}