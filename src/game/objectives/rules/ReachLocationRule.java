package game.objectives.rules;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import game.components.PlayerComponent;
import game.objectives.ObjectiveRule;

public class ReachLocationRule implements ObjectiveRule {
    private final Point targetLocation;
    private final int radius;
    private final String description;

    public ReachLocationRule(Point targetLocation, int radius, String description) {
        this.targetLocation = targetLocation;
        this.radius = radius;
        this.description = description;
    }

    @Override
    public boolean isMet(World world, EventBus eventBus) {
        var players = world.ComponentEntitiesIndex.query(PlayerComponent.class);
        if (players.isEmpty()) return false;

        EntityID player = players.stream().findFirst().get();
        PositionComponent pos = world.getEntityComponent(player, PositionComponent.class);
        if (pos == null) return false;

        double distance = pos.Origin.subtract(targetLocation).magnitude();
        return distance <= radius;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
