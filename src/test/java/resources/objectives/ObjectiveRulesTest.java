package resources.objectives;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import org.junit.jupiter.api.Test;
import resources.components.InventoryComponent;
import resources.components.PlayerComponent;
import resources.objectives.rules.AndRule;
import resources.objectives.rules.OrRule;
import resources.objectives.rules.HasItemRule;
import resources.objectives.rules.ReachLocationRule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectiveRulesTest {

    @Test
    void hasItemRuleChecksPlayerInventory() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        InventoryComponent inventory = new InventoryComponent();
        inventory.items.put("key", new InventoryComponent.Item("key", "a small key"));
        world.createEntity(new PlayerComponent(), inventory);

        HasItemRule rule = new HasItemRule("key", "has key");

        assertTrue(rule.isMet(world, bus));
        assertFalse(new HasItemRule("missing", "missing").isMet(world, bus));
    }

    @Test
    void hasItemRuleReturnsFalseWithoutInventoryOrPlayers() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        HasItemRule rule = new HasItemRule("key", "has key");

        assertFalse(rule.isMet(world, bus));

        world.createEntity(new PlayerComponent());
        assertFalse(rule.isMet(world, bus));
    }

    @Test
    void reachLocationRuleUsesRadius() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        world.createEntity(new PlayerComponent(), new PositionComponent(new Point(5, 5)));

        ReachLocationRule rule = new ReachLocationRule(new Point(7, 5), 2, "reach");

        assertTrue(rule.isMet(world, bus));
        assertFalse(new ReachLocationRule(new Point(10, 10), 2, "far").isMet(world, bus));
    }

    @Test
    void reachLocationRuleReturnsFalseWithoutPlayerOrPosition() {
        EventBus bus = new EventBus();
        World world = new World(bus);
        ReachLocationRule rule = new ReachLocationRule(new Point(1, 1), 1, "reach");

        assertFalse(rule.isMet(world, bus));

        world.createEntity(new PlayerComponent());
        assertFalse(rule.isMet(world, bus));
    }

    @Test
    void andRuleCombinesDescriptionsAndResults() {
        ObjectiveRule always = new ObjectiveRule() {
            @Override
            public boolean isMet(World world, EventBus eventBus) {
                return true;
            }

            @Override
            public String getDescription() {
                return "one";
            }
        };
        ObjectiveRule never = new ObjectiveRule() {
            @Override
            public boolean isMet(World world, EventBus eventBus) {
                return false;
            }

            @Override
            public String getDescription() {
                return "two";
            }
        };

        AndRule rule = new AndRule(always, never);

        assertFalse(rule.isMet(new World(new EventBus()), new EventBus()));
        assertTrue(rule.getDescription().contains("one"));
        assertTrue(rule.getDescription().contains("two"));
    }

    @Test
    void andRuleUsesDescriptionOverride() {
        ObjectiveRule always = new ObjectiveRule() {
            @Override
            public boolean isMet(World world, EventBus eventBus) {
                return true;
            }

            @Override
            public String getDescription() {
                return "one";
            }
        };
        ObjectiveRule never = new ObjectiveRule() {
            @Override
            public boolean isMet(World world, EventBus eventBus) {
                return false;
            }

            @Override
            public String getDescription() {
                return "two";
            }
        };

        AndRule rule = new AndRule(always, never, "custom");

        assertTrue(rule.getDescription().contains("custom"));
    }

    @Test
    void objectiveCompletesWhenAllRulesMet() {
        ObjectiveRule trueRule = new ObjectiveRule() {
            @Override
            public boolean isMet(World world, EventBus eventBus) {
                return true;
            }

            @Override
            public String getDescription() {
                return "ok";
            }
        };

        Objective obj = new Objective("test", trueRule);
        obj.update(new World(new EventBus()), new EventBus());

        assertTrue(obj.isCompleted());
    }

    @Test
    void objectiveManagerUpdatesAllObjectives() {
        ObjectiveRule trueRule = new ObjectiveRule() {
            @Override
            public boolean isMet(World world, EventBus eventBus) {
                return true;
            }

            @Override
            public String getDescription() {
                return "ok";
            }
        };

        ObjectiveManager manager = new ObjectiveManager();
        Objective obj = new Objective("test", trueRule);
        manager.addObjective(obj);
        manager.update(new World(new EventBus()), new EventBus());

        assertTrue(obj.isCompleted());
    }
}
