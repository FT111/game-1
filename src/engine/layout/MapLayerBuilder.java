package engine.layout;

import engine.Resources;
import engine.Utils;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapLayerBuilder {
    private final String mapName;
    private final LayoutManager layoutManager;
    private Predicate<HashMap<Class<? extends Component>, Component>> condition = (c) -> true;
    private BiFunction<HashMap<Class<? extends Component>, Component>, Resources, HashSet<Point>> extractor = (c, r) -> new HashSet<>();

    public MapLayerBuilder(String mapName, LayoutManager layoutManager) {
        this.mapName = mapName;
        this.layoutManager = layoutManager;
    }

    public MapLayerBuilder matching(Predicate<HashMap<Class<? extends Component>, Component>> condition) {
        this.condition = condition;
        return this;
    }

    public MapLayerBuilder extracting(BiFunction<HashMap<Class<? extends Component>, Component>, Resources, HashSet<Point>> extractor) {
        this.extractor = extractor;
        return this;
    }

    public <T extends Component> MapLayerBuilder fromTileMap(Class<T> markerComponent, Function<T, HashSet<Character>> tilesExtractor) {
        this.condition = components -> components.containsKey(markerComponent) && components.containsKey(PositionComponent.class) && components.containsKey(TileMapComponent.class) && components.containsKey(DimensionsComponent.class);
        this.extractor = (components, resources) -> {
            var marker = (T) components.get(markerComponent);
            var positionDetails = (PositionComponent) components.get(PositionComponent.class);
            var tileMapDetails = (TileMapComponent) components.get(TileMapComponent.class);
            var dimensionsComponent = (DimensionsComponent) components.get(DimensionsComponent.class);

            var tileMapAsset = resources.getAsset(tileMapDetails.resourceId, tileMapDetails.assetId, Cell[][].class);
            var targetTiles = tilesExtractor.apply(marker);

            if(targetTiles == null) {
                // If it asks for everything
                HashSet<Point> points = new HashSet<>();
                for (int y = 0; y < dimensionsComponent.width; y++) {
                    for (int x = 0; x < dimensionsComponent.height; x++) {
                        if (y >= tileMapAsset.length || x >= tileMapAsset[y].length) continue;
                        Cell cell = tileMapAsset[y][x];
                        if (cell == null || cell.content == null) continue;
                        points.add(new Point(positionDetails.Origin.x() + x, positionDetails.Origin.y() + y));
                    }
                }
                return points;
            }

            return Utils.extractTilePointsFromTileMap(tileMapDetails, dimensionsComponent, tileMapAsset, targetTiles, positionDetails);
        };
        return this;
    }

    public <T extends Component> MapLayerBuilder fromBoundingBox(Class<T> markerComponent) {
        this.condition = components -> components.containsKey(markerComponent) && components.containsKey(PositionComponent.class) && components.containsKey(DimensionsComponent.class);
        this.extractor = (components, resources) -> {
            var position = (PositionComponent) components.get(PositionComponent.class);
            var dimensions = (DimensionsComponent) components.get(DimensionsComponent.class);
            HashSet<Point> points = new HashSet<>();
            for(int x = 0; x < dimensions.width; x++) {
                for(int y = 0; y < dimensions.height; y++) {
                    points.add(new Point(position.Origin.x() + x, position.Origin.y() + y));
                }
            }
            return points;
        };
        return this;
    }

    public void register() {
         layoutManager.registerMapLayerDefinition(mapName, new MapLayerDefinition() {
             @Override
             public boolean matches(HashMap<Class<? extends Component>, Component> components) {
                 return condition.test(components);
             }
             @Override
             public HashSet<Point> extractPoints(HashMap<Class<? extends Component>, Component> components, Resources resources) {
                 return extractor.apply(components, resources);
             }
         });
    }
}
