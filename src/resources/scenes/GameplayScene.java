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
import resources.objectives.Objective;
import resources.objectives.ObjectiveManager;
import resources.objectives.ObjectiveSystem;
import resources.objectives.rules.AndRule;
import resources.objectives.rules.OrRule;
import resources.objectives.rules.HasItemRule;
import resources.objectives.rules.ReachLocationRule;

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
                new DimensionsComponent(300,300),
                new VisibilityComponent(true),
                new VisionBlockerComponent(new HashSet<>() {{
                    addAll(List.of('#', '_', '[', ']', '▁', '▎', '┴','|','▥'));
                }}),
                new LevelMapComponent(),
                new MaskToTileMapComponent(playerVision),
                new LayerColliderComponent(new HashSet<>() {{
                    addAll(List.of('#', '│', '└', '┴', '┬', '┌', '=', '─','_', '[', ']', 'O', '/', '%', '▁',
                            '▎','┴', '|'));
                }}));

        var progressiveLevelMap = world.createLayer(
                new TileMapComponent("mapAssets", "progressive-level", "tl", false),
                new PositionComponent(new Point(0,0), -1, Positioning.ABSOLUTE, Alignment.TOP_LEFT),
                new DimensionsComponent(300,300),
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
                new PositionComponent(new Point(32,22), 3),
                new RenderableComponent('@', null, null, true),
                new VelocityComponent(1.2, 8,  "exponential"),
                new VisionEmitterComponent(150, 110, 5, playerVision, new Colour(0, 40, 40)),
                new PlayerComponent(),
                new OrientationComponent(90),
                new InventoryComponent()
        );

        var guard = world.createEntity(
                new PositionComponent(new Point(15,10), 3),
                new RenderableComponent('▶', null, null, true),
                new VisionEmitterComponent(150, 110, 5, testGuardVision, new Colour(0, 30, 30)),
                new OrientationComponent(12),
                new OrientToMovementComponent(),
                new MaskToTileMapComponent(playerVision),
                new VelocityComponent(1.2, 11,  "exponential"),
                new InventoryComponent(new InventoryComponent.Item("Staff Keycard", "A staff keycard with a basic access level")),
                new GuardComponent(List.of(new Point(17, 20), new Point(52,20), new Point(30, 8)), GuardState.PATROLLING)
        );

        world.addComponentToLayer(testGuardVision, new VisionLayerComponent(guard));
        world.addComponentToLayer(testGuardVision, new TileMapComponent("vision-maps", guard.toString(), "tl", false));
        world.addComponentToLayer(playerVision, new VisionLayerComponent(player));
        world.addComponentToLayer(playerVision, new TileMapComponent("vision-maps", player.toString(), "tl", false));

        DoorSystem doorSystem = new DoorSystem(engine.Resources, engine.EventBus);

        var mainDoor = doorSystem.registerDoor(
                world,
                new Point(48, 18),
                new DoorComponent(new char[]{'['}, new char[]{' '}, 2, DoorOrientation.VERTICAL, true, true, true, "Staff Keycard")
        );

        world.addComponentToLayer(mainDoor, new MaskToTileMapComponent(playerVision));


        world.createEntity(
                new PositionComponent(new Point(26, 17), 2),
                new RenderableComponent('k', new Colour(255, 215, 0), null, true),
                new ItemComponent("Staff Keycard", "A basic keycard used by employees - Someone seems to have dropped it?"),
                new MaskToTileMapComponent(playerVision)
        );
        world.createEntity(
                new PositionComponent(new Point(4, 3), 2),
                new RenderableComponent('u', new Colour(255, 215, 0), null, true),
                new ItemComponent("USB Stick", "A USB with 'confidential' scribbled in red marker. Seems important?"),
                new MaskToTileMapComponent(playerVision)
        );

        add(doorSystem);
        add(new ChunkSystem(engine.EventBus, engine.World, 8, chunkMap));
        add(new PlayerSystem(engine.EventBus));
        add(new FogSystem(engine.Resources));
        add(new CaughtSystem(engine));

        MovementSystem movementSys = new MovementSystem(engine.EventBus);
        movementSys.movementPipeline.add(new CollisionProcessor(engine.EventBus, engine.World, engine.Resources, engine.LayoutManager));
        movementSys.movementPipeline.add(new VelocityProcessor());
        add(movementSys);

        add(new VisionSystem(engine.World, engine.Resources, engine.EventBus));

        add(new GuardAiSystem(engine.LayoutManager, new AStarPathfinder(), engine.EventBus));

        add(new InventorySystem(engine.EventBus, engine.Resources));

        ObjectiveManager objectiveManager = new ObjectiveManager();
        HasItemRule hasItem = new HasItemRule("USB Stick", "Find the USB stick with compromising material");
        ReachLocationRule escapeOpOne = new ReachLocationRule(new Point(33, 21), 1, "Escape via the main lift");
        ReachLocationRule escapeOpTwo = new ReachLocationRule(new Point(6, 20 ), 1, "Find and escape via the service lift" );
        OrRule escape = new OrRule(escapeOpOne, escapeOpTwo);
        Objective findUSB = new Objective("Find the USB",hasItem);
        Objective escapeObjective = new Objective("Escape", new AndRule(escape, hasItem, "Escape"));

        objectiveManager.addObjective(findUSB);
        objectiveManager.addObjective(escapeObjective);

        add(new ObjectiveSystem(objectiveManager, engine.EventBus));
    }

    @Override
    protected void onEnter() {
    }
}
