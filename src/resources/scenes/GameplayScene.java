package resources.scenes;

import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import resources.ChunkSystem;
import resources.PlayerSystem;
import resources.VisionSystem;

import engine.scenes.Scene;
import resources.components.VisionBlockerComponent;
import resources.components.VisionEmitterComponent;
import resources.components.VisionLayerComponent;

import java.util.HashSet;

public class GameplayScene extends Scene {

    public GameplayScene(VisionSystem visionSystem, ChunkSystem chunkSystem) {
        add(visionSystem);
        add(chunkSystem);

        var levelMap = world.createLayer(
                new TileMapComponent("mapAssets", "level", "tl", false),
                new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE),
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
        java.lang.System.out.println("Entering Gameplay Scene");
    }
}

