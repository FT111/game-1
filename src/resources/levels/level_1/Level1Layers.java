package resources.levels.level_1;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import resources.LevelContext;
import resources.LevelSection;
import resources.components.*;

import java.util.HashSet;
import java.util.List;

public class Level1Layers implements LevelSection {
    @Override
    public void run(LevelContext context) {
        context.playerVision = context.world.createLayer(
                new PositionComponent(new Point(0,0), 1),
                new VisibilityComponent(true),
                new DimensionsComponent(250,250),
                new PlayerVisionLayerComponent()
        );

        context.world.createLayer(
                new TileMapComponent("mapAssets", "level", "tl", false),
                new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE, Alignment.TOP_LEFT),
                new DimensionsComponent(300,300),
                new VisibilityComponent(true),
                new VisionBlockerComponent(new HashSet<>() {{
                    addAll(List.of('#', '_', '[', ']', '▁', '▎', '┴','|','▥'));
                }}),
                new LevelMapComponent(),
                new MaskToTileMapComponent(context.playerVision),
                new LayerColliderComponent(new HashSet<>() {{
                    addAll(List.of('#', '│', '└', '┴', '┬', '┌', '=', '─','_', '[', ']', 'O', '/', '%', '▁',
                            '▎','┴', '|'));
                }}));

        context.world.createLayer(
                new TileMapComponent("mapAssets", "progressive-level", "tl", false),
                new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE, Alignment.TOP_LEFT),
                new DimensionsComponent(300,300),
                new VisibilityComponent(true),
                new ProgressiveLevelMapComponent()
        );
    }
}
