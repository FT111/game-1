import engine.Engine;
import engine.EngineFactory;
import engine.Logs;
import engine.systems.InputHandlerSystem;
import engine.systems.MovementSystem;
import engine.systems.SceneGraphSystem;
import engine.systems.UiInteractionSystem;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.*;
import resources.*;
import resources.components.VisionEmitterComponent;
import resources.components.VisionLayerComponent;
import resources.scenes.GameplayScene;
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

        HashMap<Point, HashSet<EntityID>> chunkMap = new HashMap<>();


        var camera = engine.World.createEntity(
                new PositionComponent(new Point(0,0), 100),
                new CameraComponent(engine.Renderer.Api.getWidth(), engine.Renderer.Api.getHeight(), true)
        );
        Logs.log("Main: camera entity created");


        CameraSystem cameraSystem = new CameraSystem(camera, engine.Renderer.Api, engine.World);
        VisionSystem visionSystem = new VisionSystem(engine.World, engine.Resources, chunkMap, 1);
        ChunkSystem chunkSystem = new ChunkSystem(engine.EventBus, engine.World, 8, chunkMap);

        engine.Systems.addSystem(cameraSystem);
        engine.Systems.addSystem(visionSystem);

        engine.Systems.addSystem(chunkSystem);
        UiInteractionSystem uiSystem = new UiInteractionSystem(engine.World, engine.EventBus, engine.Resources);
        engine.Systems.addSystem(uiSystem);
        MenuSystem menu = new MenuSystem(engine.EventBus, engine.World);

        engine.Systems.addSystem(menu);
        Logs.log("Main: core gameplay systems wired");

        engine.Resources.addResourceLoader(new VisionLayerLoader(engine.World));


        // Create the scenes directly on the engine
        GameplayScene gameplay = new GameplayScene(visionSystem, chunkSystem);
        var playerVision = gameplay.world.createLayer(
                new PositionComponent(new Point(0,0), 1),
                new VisibilityComponent(true),
                new DimensionsComponent(250,250)
        );

        var player = gameplay.world.createEntity(
                new PositionComponent(new Point(3,3), 3),
                new RenderableComponent('@', null, null, true),
                new VelocityComponent(1.2, 6,  "exponential"),
                new VisionEmitterComponent(150, 110, 5, playerVision),
                new OrientationComponent(90)
        );

        gameplay.world.addComponentToLayer(playerVision, new VisionLayerComponent(player));
        gameplay.world.addComponentToLayer(playerVision, new TileMapComponent("vision-maps", player.toString(), "tl", false));

        var playerSystem = new PlayerSystem(engine.EventBus, player, camera);
        gameplay.add(playerSystem);
        engine.Systems.addSystem(playerSystem);

        engine.SceneManager
                .addScene("MainMenu", new MainMenuScene(menu, uiSystem)
                        .add(engine.Systems.getSystem(InputHandlerSystem.class))
                        .add(engine.Systems.getSystem(UiInteractionSystem.class))
                        .add(cameraSystem)
                        .add(menu))
                .addScene("Gameplay", gameplay
                        .add(engine.Systems.getSystem(InputHandlerSystem.class))
                        .add(engine.Systems.getSystem(MovementSystem.class))
                        .add(engine.Systems.getSystem(SceneGraphSystem.class))
                        .add(engine.Systems.getSystem(VisionSystem.class))
                        .add(chunkSystem)
                        .add(uiSystem)
                        .add(cameraSystem)
                        .add(menu));
        Logs.log("Main: scenes registered");

        // Switch to default scene
        engine.SceneManager.switchScene("MainMenu");
        Logs.log("Main: switched to Gameplay scene");
        Logs.log("Main: entering game loop");
        engine.StartGameLoop();

    }
}
