import engine.Engine;
import engine.EngineFactory;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.*;
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

        var levelMap = engine.World.createLayer();
        engine.World.addComponentToLayer(levelMap, new TileMapComponent("mapAssets", "level", "tl", false, true, 100, 100));
        engine.World.addComponentToLayer(levelMap, new PositionComponent(new Point(0,0), -1));
        engine.World.addComponentToLayer(levelMap, new VisionBlockerComponent(new HashSet<>() {{
            add('#');
        }}));
        engine.World.addComponentToLayer(levelMap, new LayerColliderComponent(new HashSet<>() {{
            add('#');
        }}));
        var camera = engine.World.createEntity();
        engine.World.addComponentToEntity(camera, new PositionComponent(new Point(0,0), 100));
        engine.World.addComponentToEntity(camera, new engine_interfaces.objects.components.CameraComponent(engine.Renderer.Api.getWidth(), engine.Renderer.Api.getHeight(), true));
        var player = engine.World.createEntity();
        var playerVision = engine.World.createLayer();
        engine.World.addComponentToEntity(player, new PositionComponent(new Point(3,3), 3));
        engine.World.addComponentToEntity(player, new RenderableComponent('@', null, null, true));
        engine.World.addComponentToEntity(player, new VelocityComponent(1.2, 6,  "exponential"));
        engine.World.addComponentToEntity(player, new VisionEmitterComponent(150, 140, 5, playerVision));
        engine.World.addComponentToEntity(player, new OrientationComponent(90));

        engine.World.addComponentToLayer(playerVision, new PositionComponent(new Point(0,0), 1));
        engine.World.addComponentToLayer(playerVision, new VisionLayerComponent(player));
        engine.World.addComponentToLayer(playerVision, new TileMapComponent("vision-maps", player.toString(), "tl", false, true, 200, 200));

        var testText = engine.World.createLayer();
        engine.World.addComponentToLayer(testText, new PositionComponent(new Point(3,3), 1, true));
        engine.World.addComponentToLayer(testText, new TextComponent("Hello, World!"));

        engine.Systems.addSystem(new TestSystem(camera, engine.Renderer.Api, engine.World));
        engine.Systems.addSystem(new VisionSystem(engine.World, engine.Resources, chunkMap, 1));
        PlayerSystem playerSystem = new PlayerSystem(engine.EventBus, engine.World, player, camera);
        engine.Renderer.Api.onResize(() -> {
            playerSystem.lockCameraToPlayer(engine.World, camera);
        });
        engine.Systems.addSystem(playerSystem);
        engine.Systems.addSystem(new ChunkSystem(engine.EventBus, engine.World, 8, chunkMap));

        engine.Resources.addResourceLoader(new VisionLayerLoader(engine.World));

        engine.StartGameLoop();
    }
}
