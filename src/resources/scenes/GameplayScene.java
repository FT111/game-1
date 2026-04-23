package resources.scenes;

import engine_interfaces.objects.System;
import resources.ChunkSystem;
import resources.PlayerSystem;
import resources.TestSystem;
import resources.VisionSystem;

import engine.scenes.Scene;

public class GameplayScene extends Scene {

    public GameplayScene(VisionSystem visionSystem, PlayerSystem playerSystem, ChunkSystem chunkSystem) {
        addSystem(visionSystem);
        addSystem(playerSystem);
        addSystem(chunkSystem);

    }

    @Override
    protected void onEnter() {
        java.lang.System.out.println("Entering Gameplay Scene");
    }
}

