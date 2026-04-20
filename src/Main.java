import engine.Engine;
import engine.EngineFactory;
import engine.systems.UiInteractionSystem;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.MouseEventTypes;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.components.ui.ButtonComponent;
import engine_interfaces.objects.components.ui.UIElementComponent;
import engine_interfaces.objects.events.KeyInputEvent;
import engine_interfaces.objects.events.MouseInputEvent;
import engine_interfaces.objects.ui.SelectionStrategies;
import resources.*;
import resources.components.VisionBlockerComponent;
import resources.components.VisionEmitterComponent;
import resources.components.VisionLayerComponent;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) throws IOException {
        EngineFactory factory = new EngineFactory();
        Engine engine = factory.build();

        engine.Resources.addResourceLoader(new MapAssetLoader());

        HashMap<Point, HashSet<EntityID>> chunkMap = new HashMap<>();

        var levelMap = engine.World.createLayer(
            new TileMapComponent("mapAssets", "level", "tl", false),
            new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE),
            new DimensionsComponent(100,100),
            new VisibilityComponent(true),
            new VisionBlockerComponent(new HashSet<>() {{
            add('#');
        }}),
            new LayerColliderComponent(new HashSet<>() {{
            add('#');
        }})
        );
        var camera = engine.World.createEntity(
            new PositionComponent(new Point(0,0), 100),
            new CameraComponent(engine.Renderer.Api.getWidth(), engine.Renderer.Api.getHeight(), true)
        );

        var playerVision = engine.World.createLayer(
            new PositionComponent(new Point(0,0), 1),
            new VisibilityComponent(true),
            new DimensionsComponent(250,250)
        );

        var player = engine.World.createEntity(
            new PositionComponent(new Point(3,3), 3),
            new RenderableComponent('@', null, null, true),
            new VelocityComponent(1.2, 6,  "exponential"),
            new VisionEmitterComponent(150, 110, 5, playerVision),
            new OrientationComponent(90)
        );

        engine.World.addComponentToLayer(playerVision, new VisionLayerComponent(player));
        engine.World.addComponentToLayer(playerVision, new TileMapComponent("vision-maps", player.toString(), "tl", false));

        var testButton = engine.World.createLayer(
            new PositionComponent(new Point(3,5), 1, Positioning.FIXED),
            new DimensionsComponent(5, 3),
            new UIElementComponent(SelectionStrategies.BOUNDING),
            new VisibilityComponent(true),
            new TextComponent("Click")
        );

        engine.Systems.addSystem(new TestSystem(camera, engine.Renderer.Api, engine.World));
        engine.Systems.addSystem(new VisionSystem(engine.World, engine.Resources, chunkMap, 1));
        PlayerSystem playerSystem = new PlayerSystem(engine.EventBus, engine.World, player, camera);
        engine.Renderer.Api.onResize(() -> {
            playerSystem.lockCameraToPlayer(engine.World, camera);
        });
        engine.Systems.addSystem(playerSystem);
        engine.Systems.addSystem(new ChunkSystem(engine.EventBus, engine.World, 8, chunkMap));
        engine.Systems.addSystem(new UiInteractionSystem(engine.World, engine.EventBus, engine.Resources));
        MenuSystem menu = new MenuSystem(engine.EventBus, engine.World);

        engine.Systems.addSystem(menu);

//        engine.EventBus.subscribe(KeyInputEvent.class,"Main", event -> {
//            var keyEvent = (KeyInputEvent) event;
//
//            if (keyEvent.key == 'q') {
//                IO.println("Publishing 20,15 mouse event");
//                engine.EventBus.publish(new MouseInputEvent(new Point(20, 15), MouseEventTypes.DOWN));
//            }
//        });

        engine.Resources.addResourceLoader(new VisionLayerLoader(engine.World));

        engine.StartGameLoop();

    }
}
