package resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine_interfaces.objects.rendering.Cell;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class MapAssetLoader implements engine_interfaces.objects.ResourceLoader {
    public static class JsonMapDefinition {
        public String name;
        public Map<String, String> legend;
        public List<Object> rows;
    }

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

            // check if flat or nested
            List<String> flatRows = new ArrayList<>();
            if (def.rows.get(0) instanceof List) {
                // Nested structure: rows[0] is a list of strings
                for (Object rowArray : def.rows) {
                    if (rowArray instanceof List<?> rowList) {
                        for (Object row : rowList) {
                            if (row instanceof String rowString) {
                                flatRows.add(rowString);
                            }
                        }
                    }
                }
            } else {
                // Flat structure: rows is already a list of strings
                for (Object row : def.rows) {
                    if (row instanceof String rowString) {
                        flatRows.add(rowString);
                    }
                }
            }

            int height = flatRows.size();
            int width = flatRows.stream().mapToInt(String::length).max().orElse(0);
            Cell[][] map = new Cell[height][width];

            for (int y = 0; y < height; y++) {
                String row = flatRows.get(y);

                for (int x = 0; x < width; x++) {
                    char tile = (x < row.length()) ? row.charAt(x) : ' ';

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
        Cell[][] level1 = loadMapFromJsonResource("maps/level_1.json");
        return new HashMap<>() {{
            put("level", level1);
            put("progressive-level", new Cell[100][100]);
        }};
    }

    @Override
    public void Save() { }

    @Override
    public boolean isWritable() {
        return false;
    }
}