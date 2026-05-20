package resources;

import engine.CoreWorld;
import engine.EventBus;
import engine.Resources;
import engine.World;
import engine.layout.LayoutManager;
import engine.systems.MovementSystem;
import engine.systems.movement.CollisionProcessor;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.LayerColliderComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.events.CollisionEvent;
import engine_interfaces.objects.events.MovementProposalEvent;
import org.junit.jupiter.api.Test;
import resources.components.InventoryComponent;
import resources.components.ItemComponent;
import resources.components.PlayerComponent;
import resources.objectives.Objective;
import resources.objectives.ObjectiveManager;
import resources.objectives.ObjectiveSystem;
import resources.objectives.rules.OrRule;
import resources.objectives.rules.HasItemRule;
import resources.objectives.rules.ReachLocationRule;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameplayIntegrationTest {

    @Test
    void movementPickupCompletesObjectiveAndUpdatesHud() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        Resources resources = new Resources();

        EntityID player = world.createEntity(
                new PlayerComponent(),
                new InventoryComponent(),
                new PositionComponent(new Point(0, 0))
        );
        EntityID item = world.createEntity(
                new ItemComponent("TestItem", "TestDescription"),
                new PositionComponent(new Point(1, 1))
        );

        InventorySystem inventorySystem = new InventorySystem(bus, resources);
        inventorySystem.onEnter(world);

        ObjectiveManager manager = new ObjectiveManager();
        HasItemRule hasItem = new HasItemRule("TestItem", "TestDescription");
        ReachLocationRule reachExit = new ReachLocationRule(new Point(2, 2), 0, "Reach the exit");
        Objective objective = new Objective("Escape", new OrRule(hasItem, reachExit, "TestDescription"));
        manager.addObjective(objective);

        ObjectiveSystem objectiveSystem = new ObjectiveSystem(manager, bus);
        objectiveSystem.onEnter(world);

        MovementSystem movementSystem = new MovementSystem(bus);
        movementSystem.onEnter(world);

        bus.publish(new MovementProposalEvent(player, new Point(1, 1), new Point(0, 0)));
        bus.flush();
        movementSystem.update(world, 1);
        bus.flush();

        InventoryComponent inventory = world.getEntityComponent(player, InventoryComponent.class);
        assertEquals(1, inventory.items.size());
        assertEquals("TestItem", inventory.items.get("TestItem").itemName);
        assertFalse(world.Entities.containsKey(item));

        bus.publish(new MovementProposalEvent(player, new Point(2, 2), new Point(1, 1)));
        bus.flush();
        movementSystem.update(world, 2);
        bus.flush();

        objectiveSystem.update(world, 2);

        assertTrue(objective.isCompleted());

        TextComponent hudText = world.Layers.values().stream()
                .map(components -> CoreWorld.getComponent(components, TextComponent.class))
                .filter(Objects::nonNull)
                .filter(component -> component.text != null && component.text.contains("Escape"))
                .findFirst()
                .orElse(null);
        assertNotNull(hudText);
        assertTrue(hudText.text.contains("Escape (COMPLETED)"));
        assertTrue(hudText.text.contains("[x]"));
    }

    @Test
    void collisionPreventsPickupAndMovement() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        Resources resources = new Resources();
        LayoutManager layoutManager = new LayoutManager(world, bus, resources);
        layoutManager.defineMapLayer("collision")
                .fromBoundingBox(LayerColliderComponent.class)
                .register();

        LayerID blockingLayer = world.createLayer(
                new PositionComponent(new Point(1, 1)),
                new DimensionsComponent(1, 1),
                new LayerColliderComponent(new HashSet<>())
        );
        bus.flush();

        EntityID player = world.createEntity(
                new PlayerComponent(),
                new InventoryComponent(),
                new PositionComponent(new Point(0, 0))
        );
        EntityID item = world.createEntity(
                new ItemComponent("Keycard", "An access keycard"),
                new PositionComponent(new Point(1, 1))
        );

        InventorySystem inventorySystem = new InventorySystem(bus, resources);
        inventorySystem.onEnter(world);

        AtomicInteger collisions = new AtomicInteger(0);
        bus.subscribe(CollisionEvent.class, () -> true, event -> collisions.incrementAndGet());

        MovementSystem movementSystem = new MovementSystem(bus);
        movementSystem.movementPipeline.add(new CollisionProcessor(bus, world, resources, layoutManager));
        movementSystem.onEnter(world);

        bus.publish(new MovementProposalEvent(player, new Point(1, 1), new Point(0, 0)));
        bus.flush();
        movementSystem.update(world, 1);
        bus.flush();

        PositionComponent position = world.getEntityComponent(player, PositionComponent.class);
        InventoryComponent inventory = world.getEntityComponent(player, InventoryComponent.class);
        assertEquals(new Point(0, 0), position.Origin);
        assertEquals(0, inventory.items.size());
        assertTrue(world.Entities.containsKey(item));
        assertEquals(1, collisions.get());

        world.removeLayer(blockingLayer);
    }
}
