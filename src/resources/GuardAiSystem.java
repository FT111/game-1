package resources;

import engine.EventBus;
import engine.World;
import engine.layout.LayoutManager;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.events.MovementEvent;
import engine_interfaces.objects.events.MovementProposalEvent;
import resources.components.GuardComponent;
import resources.movement.PathfindingStrategy;

import java.lang.reflect.Array;
import java.util.*;

public class GuardAiSystem extends System {
    private final LayoutManager layoutManager;
    private final PathfindingStrategy pathfinder;
    private final EventBus bus;
    private final Map<EntityID, MovementProposalEvent> pendingMovements = new HashMap<>();

    public GuardAiSystem(LayoutManager layoutManager, PathfindingStrategy pathfinder, EventBus bus) {
        this.layoutManager = layoutManager;
        this.pathfinder = pathfinder;
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        super.onEnter(world);

        // checks if a guard moves. if so, removes its pending movment to let it move to the next routed node
        bus.subscribe(MovementEvent.class, () -> isEnabled, event -> {
            var movementEvent = (MovementEvent) event;
            pendingMovements.remove(movementEvent.entityID);
        });
    }

    /// Uses a routing algorithm to route between two points, taking into account the spatial map of the world and any
    ///  collidable obstacles.
    public List<Point> routeBetween(Point from, Point to) {
        return pathfinder.findPath(from, to, layoutManager.getSpatialMap("collision"));
    }

    @Override
    public void update(World world, int tickCount) {
        super.update(world, tickCount);

        var guards = (HashSet<EntityID>) world.ComponentEntitiesIndex.query(new Class[] {GuardComponent.class, PositionComponent.class});
        for (var guardID : guards) {
            GuardComponent guardComp = (GuardComponent)world.Entities.get(guardID).get(GuardComponent.class);
            PositionComponent positionComp = (PositionComponent)world.Entities.get(guardID).get(PositionComponent.class);
            if (guardComp == null) continue; // shouldn't happen but can't hurt

            if (guardComp.mainPathPoints != null) {
                if (guardComp.mainPathPoints.peek() != null && guardComp.mainPathPoints.peek().equals(positionComp.Origin)) {
                    guardComp.mainPathPoints.pop();
                }

                if (guardComp.interpolatedPathPoints != null && guardComp.interpolatedPathPoints.peek() == null) {
                    guardComp.interpolatedPathPoints = new Stack<>();
                    List<Point> nextPathSegment = routeBetween(positionComp.Origin, guardComp.mainPathPoints.peek());
                    for (int i = nextPathSegment.size() - 1; i >= 0; i--) {
                        guardComp.interpolatedPathPoints.push(nextPathSegment.get(i));
                    }
                }

                if (guardComp.interpolatedPathPoints != null && guardComp.interpolatedPathPoints.peek() != null) {
                    var nextPoint = guardComp.interpolatedPathPoints.pop();
                    MovementProposalEvent movementProposalEvent = new MovementProposalEvent(guardID, nextPoint, positionComp.Origin, true, null);

                    if (pendingMovements.get(guardID) == null || !pendingMovements.get(guardID).equals(movementProposalEvent)) {
                        bus.publish(movementProposalEvent);
                        pendingMovements.put(guardID, movementProposalEvent);
                    }
                }
            }
        }
    }
}
