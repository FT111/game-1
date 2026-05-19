package resources;

import engine.Logs;
import engine.Resources;
import engine.World;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import resources.components.LevelMapComponent;
import resources.components.PlayerVisionLayerComponent;
import resources.components.ProgressiveLevelMapComponent;

/// updates the progressive vision map to store all the areas of the map where the player has seen
public class FogSystem extends System {
    private final Resources resources;

    public FogSystem(Resources resources) {
        this.resources = resources;
    }

    private Cell[][] extractTileMap(World world, LayerID layerId) {
        var tileMapComp = (TileMapComponent) world.Layers.get(layerId).get(TileMapComponent.class);
        try {
            return resources.getAsset(tileMapComp.resourceId, tileMapComp.assetId, Cell[][].class);
        } catch (IllegalArgumentException ex) {
            return new Cell[200][200];
        }
    }

    @Override
    public void update(World world, int tickCount) {
        super.update(world, tickCount);

        var playerVision = world.ComponentLayersIndex.query(PlayerVisionLayerComponent.class).stream().findFirst();
        var progressiveTileMapLayer = world.ComponentLayersIndex.query(ProgressiveLevelMapComponent.class).stream().findFirst();
        var levelMap = world.ComponentLayersIndex.query(LevelMapComponent.class).stream().findFirst();

        if (playerVision.isEmpty() || progressiveTileMapLayer.isEmpty() || levelMap.isEmpty()) {return;}

        var playerVisionPosition = ((PositionComponent)world.Layers.get(playerVision.get()).get(PositionComponent.class)).Origin;
        var playerVisionTileMap = extractTileMap(world, playerVision.get());
        var progressiveTileMap = extractTileMap(world, progressiveTileMapLayer.get());
        var levelMapTileMap = extractTileMap(world, levelMap.get());

        for (int y = 0; y < playerVisionTileMap.length; y++) {
            for (int x = 0; x < playerVisionTileMap[y].length; x++) {
                int worldX = playerVisionPosition.x() + x;
                int worldY = playerVisionPosition.y() + y;

                // validate bounds against levelMapTileMap instead of playerVisionTileMap
                if (worldX < 0 || worldY < 0 || worldY >= levelMapTileMap.length || worldX >= levelMapTileMap[worldY].length) {
                    continue;
                }

                // also validate bounds for progressiveTileMap
                if (worldY >= progressiveTileMap.length || worldX >= progressiveTileMap[worldY].length) {
                    continue;
                }

                // if it's not null, place the level tile on the progressive map
                if (playerVisionTileMap[y][x] != null && levelMapTileMap[worldY][worldX] != null) {
                    progressiveTileMap[worldY][worldX] = levelMapTileMap[worldY][worldX];
                }
            }
        }
    }
}
