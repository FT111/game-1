package resources;

import engine.World;
import engine.layout.LayoutManager;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import resources.components.GuardComponent;
import resources.movement.PathfindingStrategy;

import java.util.List;

public class GuardAiSystem extends System {
    private final LayoutManager layoutManager;
    private final PathfindingStrategy pathfinder;

    public GuardAiSystem(LayoutManager layoutManager, PathfindingStrategy pathfinder) {
        this.layoutManager = layoutManager;
        this.pathfinder = pathfinder;
    }

    @Override
    public void onEnter(World world) {
        super.onEnter(world);

    }

    /// Uses a routing algorithm to route between two points, taking into account the spatial map of the world and any
    ///  collidable obstacles.
    public List<Point> routeBetween(Point from, Point to) {
        
        return List.of();
    }

    @Override
    public void update(World world, int tickCount) {
        super.update(world, tickCount);

        var guards = world.ComponentEntitiesIndex.query(GuardComponent.class);
    }
}
