package resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.Colour;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
            '#', '.', 'D', 'G', 'R', 'd', 'c', 'f', 'x', 'p', 'k', 'b', 'E', 'S', 't', 's', ' '
    );

    public Cell[][] loadMapFromJsonFile(String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            JsonMapDefinition def = mapper.readValue(json, JsonMapDefinition.class);

            if (def.rows == null || def.rows.isEmpty()) {
                throw new IllegalArgumentException("Map has no rows: " + filePath);
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
                    map[y][x] = new Cell(tile, null, new Colour(0, 15, 15));
                }
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load map JSON: " + filePath, e);
        }
    }

    @Override
    public String getKey() {
        return "mapAssets";
    }

    @Override
    public HashMap<String, Object> Load() {
        Cell[][] office80s = loadMapFromJsonFile("src/resources/maps/office_level_80s.json");
        return new HashMap<>() {{
            put("level", office80s);          // keeps Main.java compatible
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