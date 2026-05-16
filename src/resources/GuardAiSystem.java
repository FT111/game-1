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
import resources.events.PlayerSpottedEvent; // added
import resources.ai.bt.*;
import resources.components.GuardComponent;
import resources.movement.PathfindingStrategy;

import java.util.*;

public class GuardAiSystem extends System {
    private final LayoutManager layoutManager;
    private final PathfindingStrategy pathfinder;
    public final EventBus bus;
    private final Map<EntityID, MovementProposalEvent> pendingMovements = new HashMap<>();
    private final Map<EntityID, Point> lastKnownPlayerPos = new HashMap<>(); // added

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

        //
        bus.subscribe(PlayerSpottedEvent.class, () -> isEnabled, event -> {
            var spottedEvent = (PlayerSpottedEvent) event;
            lastKnownPlayerPos.put(spottedEvent.spotterId, spottedEvent.playerPosition);
        });
    }

    /// Uses a routing algorithm to route between two points, taking into account the spatial map of the world and any
    ///  collidable obstacles.
    public List<Point> routeBetween(Point from, Point to) {
        return pathfinder.findPath(from, to, layoutManager.getSpatialMap("collision"));
    }

    public MovementProposalEvent getPendingMovement(EntityID entityID) {
        return pendingMovements.get(entityID);
    }

    public Point getLastKnownPlayerPos(EntityID guardID) {
        return lastKnownPlayerPos.get(guardID);
    }

    public void clearLastKnownPlayerPos(EntityID guardID) {
        lastKnownPlayerPos.remove(guardID);
    }

    public void proposeMovement(EntityID entityID, Point origin, Point target) {
        MovementProposalEvent movementProposalEvent = new MovementProposalEvent(entityID, origin, target, true, null);
        if (pendingMovements.get(entityID) == null || !pendingMovements.get(entityID).equals(movementProposalEvent)) {
            bus.publish(movementProposalEvent);
            pendingMovements.put(entityID, movementProposalEvent);
        }
    }

    @Override
    public void update(World world, int tickCount) {
        super.update(world, tickCount);

        var guards = (HashSet<EntityID>) world.ComponentEntitiesIndex.query(new Class[] {GuardComponent.class, PositionComponent.class});
        for (var guardID : guards) {
            GuardComponent guardComp = (GuardComponent)world.Entities.get(guardID).get(GuardComponent.class);
            PositionComponent positionComp = (PositionComponent)world.Entities.get(guardID).get(PositionComponent.class);
            if (guardComp == null) continue;

            if (guardComp.behaviourTree == null) {
                // base behaviour tree node
                // runs top to bottom, branching if a node sequence evaluates to a success.
                guardComp.behaviourTree = new Selector(Arrays.asList(
                    new Sequence(Arrays.asList(
                            new IsPlayerCatchable(this),
                            new CatchPlayerNode(this)
                    )),
                    new Sequence(Arrays.asList(
                        new IsPlayerSpottedNode(this),
                        new ChasePlayerNode(this)
                    )),
                    new PatrolNode(this)
                ));
            }

            guardComp.behaviourTree.evaluate(world, guardID);
        }
    }

    public void routeIfNecessary(GuardComponent guardComp, PositionComponent positionComp) {
        if (guardComp.interpolatedPathPoints != null) {
            try {
                guardComp.mainPathPoints.peek();
            } catch (EmptyStackException e) {
                return; // if no more main path points, don't route
            }

            // check if empty
            try {
                guardComp.interpolatedPathPoints.peek();
            } catch (EmptyStackException e) {
                // if empty, give a route
                guardComp.interpolatedPathPoints = new Stack<>();
                List<Point> nextPathSegment = routeBetween(positionComp.Origin, guardComp.mainPathPoints.peek());
                for (int i = nextPathSegment.size() - 1; i >= 0; i--) {
                    guardComp.interpolatedPathPoints.push(nextPathSegment.get(i));
                }
            }

        }
    }
}
