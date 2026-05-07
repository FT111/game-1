package resources.scenes;

import engine.Engine;
import engine.EventBus;
import engine.systems.MovementSystem;
import engine.systems.movement.CollisionProcessor;
import engine.systems.movement.VelocityProcessor;
import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import resources.ChunkSystem;
import resources.PlayerSystem;
import resources.VisionSystem;

import engine.scenes.Scene;
import resources.components.PlayerComponent;
import resources.components.VisionBlockerComponent;
import resources.components.VisionEmitterComponent;
import resources.components.VisionLayerComponent;

import java.util.HashMap;
import java.util.HashSet;

public class GameplayScene extends Scene {

    public GameplayScene(Engine engine) {

        HashMap<Point, HashSet<EntityID>> chunkMap = new HashMap<>();

        var playerVision = world.createLayer(
                new PositionComponent(new Point(0,0), 1),
                new VisibilityComponent(true),
                new DimensionsComponent(250,250)
        );

        var player = world.createEntity(
                new PositionComponent(new Point(3,3), 3),
                new RenderableComponent('@', null, null, true),
                new VelocityComponent(1.2, 6,  "exponential"),
                new VisionEmitterComponent(150, 110, 5, playerVision),
                new PlayerComponent(),
                new OrientationComponent(90)
        );

        world.addComponentToLayer(playerVision, new VisionLayerComponent(player));
        world.addComponentToLayer(playerVision, new TileMapComponent("vision-maps", player.toString(), "tl", false));


        add(new VisionSystem(engine.World, engine.Resources, chunkMap, 1));
        add(new ChunkSystem(engine.EventBus, engine.World, 8, chunkMap));
        add(new PlayerSystem(engine.EventBus));
        MovementSystem movementSys = new MovementSystem(engine.EventBus);
        movementSys.movementPipeline.add(new CollisionProcessor(engine.EventBus, engine.World, engine.Resources));
        movementSys.movementPipeline.add(new VelocityProcessor());
        add(movementSys);

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

