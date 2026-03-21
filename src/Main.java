import engine.Engine;
import engine.EngineFactory;
import engine.rendering.TileMapRenderPass;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.RenderableComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import resources.MapAssetLoader;
import resources.PlayerSystem;
import resources.TestSystem;

public class Main {
    public static void main(String[] args) {
        EngineFactory factory = new EngineFactory();
        Engine engine = factory.build();


        engine.Resources.addResourceLoader(new MapAssetLoader());


        var levelMap = engine.World.createLayer();
        engine.World.addComponentToLayer(levelMap, new TileMapComponent("mapAssets", "level", "tl", false, true));
        engine.World.addComponentToLayer(levelMap, new PositionComponent(new Point(0,0)));
        var camera = engine.World.createEntity();
        engine.World.addComponentToEntity(camera, new PositionComponent(new Point(0,0)));
        engine.World.addComponentToEntity(camera, new engine_interfaces.objects.components.CameraComponent(engine.Renderer.Api.getWidth(), engine.Renderer.Api.getHeight(), true));
        var player = engine.World.createEntity();
        engine.World.addComponentToEntity(player, new PositionComponent(new Point(8,8)));
        engine.World.addComponentToEntity(player, new RenderableComponent('@', null, null, true));

        engine.Systems.addSystem(new TestSystem(camera, engine.Renderer.Api, engine.World));
        engine.Systems.addSystem(new PlayerSystem(engine.EventBus, engine.World, player, camera));
        engine.StartGameLoop();
    }
}
