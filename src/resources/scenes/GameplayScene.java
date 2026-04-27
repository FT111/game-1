package resources.scenes;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import resources.ChunkSystem;
import resources.VisionSystem;

import engine.scenes.Scene;
import resources.components.VisionBlockerComponent;

import java.util.HashSet;

public class GameplayScene extends Scene {

    public GameplayScene(VisionSystem visionSystem, ChunkSystem chunkSystem) {
        add(visionSystem);
        add(chunkSystem);

        var levelMap = world.createLayer(
                new TileMapComponent("mapAssets", "level", "tl", false),
                new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE, Alignment.TOP_LEFT),
                new DimensionsComponent(100,100),
                new VisibilityComponent(true),
                new VisionBlockerComponent(new HashSet<>() {{
                    add('#');
                }}),
                new LayerColliderComponent(new HashSet<>() {{
                    add('#');
                }}));


    }



    @Override
    protected void onEnter() {
    }
}

