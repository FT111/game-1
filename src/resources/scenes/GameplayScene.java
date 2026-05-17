package resources.scenes;

import engine.Engine;
import engine.systems.MovementSystem;
import engine.systems.movement.CollisionProcessor;
import engine.systems.movement.VelocityProcessor;
import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.rendering.Colour;
import resources.*;

import engine.scenes.Scene;
import resources.ai.GuardState;
import resources.components.*;
import resources.movement.pathfinders.AStarPathfinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GameplayScene extends Scene {

    public GameplayScene(Engine engine) {

        HashMap<Point, HashSet<EntityID>> chunkMap = new HashMap<>();

        var playerVision = world.createLayer(
                new PositionComponent(new Point(0,0), 1),
                new VisibilityComponent(true),
                new DimensionsComponent(250,250),
                new PlayerVisionLayerComponent()
        );

        var levelMap = world.createLayer(
                new TileMapComponent("mapAssets", "level", "tl", false),
                new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE, Alignment.TOP_LEFT),
                new DimensionsComponent(100,100),
                new VisibilityComponent(true),
                new VisionBlockerComponent(new HashSet<>() {{
                    add('#');
                }}),
                new LevelMapComponent(),
                new MaskToTileMapComponent(playerVision),
                new LayerColliderComponent(new HashSet<>() {{
                    add('#');
                }}));

        var progressiveLevelMap = world.createLayer(
                new TileMapComponent("mapAssets", "progressive-level", "tl", false),
                new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE, Alignment.TOP_LEFT),
                new DimensionsComponent(100,100),
                new VisibilityComponent(true),
                new ProgressiveLevelMapComponent()
        );

        var testGuardVision = world.createLayer(
                new PositionComponent(new Point(0,0), 2),
                new VisibilityComponent(true),
                new DimensionsComponent(250,250),
                new MaskToTileMapComponent(playerVision)
        );

        var player = world.createEntity(
                new PositionComponent(new Point(3,3), 3),
                new RenderableComponent('@', null, null, true),
                new VelocityComponent(1.2, 9,  "exponential"),
                new VisionEmitterComponent(150, 110, 5, playerVision, new Colour(0, 40, 40)),
                new PlayerComponent(),
                new OrientationComponent(90)
        );

        var guard = world.createEntity(
                new PositionComponent(new Point(5,7), 3),
                new RenderableComponent('*', null, null, true),
                new VisionEmitterComponent(150, 110, 5, testGuardVision, new Colour(0, 30, 30)),
                new OrientationComponent(12),
                new OrientToMovementComponent(),
                new MaskToTileMapComponent(playerVision),
                new VelocityComponent(1.2, 9,  "exponential"),
                new GuardComponent(List.of(new Point(25, 7), new Point(45, 40)), GuardState.PATROLLING)
        );

        world.addComponentToLayer(testGuardVision, new VisionLayerComponent(guard));
        world.addComponentToLayer(testGuardVision, new TileMapComponent("vision-maps", guard.toString(), "tl", false));
        world.addComponentToLayer(playerVision, new VisionLayerComponent(player));
        world.addComponentToLayer(playerVision, new TileMapComponent("vision-maps", player.toString(), "tl", false));


        add(new ChunkSystem(engine.EventBus, engine.World, 8, chunkMap));
        add(new PlayerSystem(engine.EventBus));
        add(new FogSystem(engine.Resources));

        MovementSystem movementSys = new MovementSystem(engine.EventBus);
        movementSys.movementPipeline.add(new CollisionProcessor(engine.EventBus, engine.World, engine.Resources, engine.LayoutManager));
        movementSys.movementPipeline.add(new VelocityProcessor());
        add(movementSys);

        add(new VisionSystem(engine.World, engine.Resources, engine.EventBus));

        add(new GuardAiSystem(engine.LayoutManager, new AStarPathfinder(), engine.EventBus));



    }

    @Override
    protected void onEnter() {
    }
}
