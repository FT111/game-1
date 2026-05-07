import engine.Engine;
import engine.EngineFactory;
import engine.Logs;
import engine.systems.InputHandlerSystem;
import engine.systems.MovementSystem;
import engine.systems.SceneGraphSystem;
import engine.systems.UiInteractionSystem;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.*;
import resources.*;
import resources.components.VisionEmitterComponent;
import resources.components.VisionLayerComponent;
import resources.scenes.GameplayScene;
import resources.scenes.GlobalScene;
import resources.scenes.MainMenuScene;

import java.util.HashMap;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        Logs.log("Main: startup begin");
        EngineFactory factory = new EngineFactory();
        Logs.log("Main: building engine");
        Engine engine = factory.build();
        Logs.log("Main: engine built");

        engine.Resources.addResourceLoader(new MapAssetLoader());

        MenuSystem menu = new MenuSystem(engine.EventBus, engine.World);

        engine.Systems.addSystem(menu);
        Logs.log("Main: core gameplay systems wired");

        engine.Resources.addResourceLoader(new VisionLayerLoader(engine.World));

        engine.SceneManager
                .addScene("Global", new GlobalScene(engine))
                .addScene("MainMenu", new MainMenuScene())
                .addScene("Gameplay", new GameplayScene(engine));
        Logs.log("Main: scenes registered");

        // Switch to default scene
        engine.SceneManager.switchScene("Global");
        engine.SceneManager.pushScene("MainMenu");
        Logs.log("Main: entering game loop");
        engine.StartGameLoop();

    }
}
