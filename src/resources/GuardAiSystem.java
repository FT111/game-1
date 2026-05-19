package resources;

import engine.EventBus;
import engine.Logs;
import engine.World;
import engine.layout.LayoutManager;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.OrientationComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.RenderableComponent;
import engine_interfaces.objects.events.MovementEvent;
import engine_interfaces.objects.events.MovementProposalEvent;
import engine_interfaces.objects.events.NewGameEvent;
import resources.ai.GuardAlertness;
import resources.events.PlayerSpottedEvent; // added
import resources.events.GlobalAlertnessChangedEvent;
import resources.events.PlayerSeenEvent;
import resources.events.AwarenessChangedEvent;
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
    // awareness per guard -> per player
    private final Map<EntityID, Map<EntityID, Integer>> awareness = new HashMap<>();
    // track players seen during the current tick (set by PlayerSeenEvent handler)
    private final Map<EntityID, java.util.HashSet<EntityID>> seenThisTick = new HashMap<>();
    private final int AWARENESS_THRESHOLD = 40; // configurable number of sight ticks to fully spot
    private final int AWARENESS_DECAY_PER_TICK = 1; // how many awareness points to decay per tick when not seen
    // global alertness level shared across all guards
    public GuardAlertness globalAlertness = GuardAlertness.RELAXED;
    // how long ALERT persists (in ticks) after a guard fully spots the player
    private final int ALERT_PERSIST_TICKS = 60*90;
    private int alertTimer = 0;
    private engine_interfaces.objects.EventSubscriptionReceipt newGameSubscription;

    public GuardAiSystem(LayoutManager layoutManager, PathfindingStrategy pathfinder, EventBus bus) {
        this.layoutManager = layoutManager;
        this.pathfinder = pathfinder;
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        super.onEnter(world);

        newGameSubscription = bus.subscribe(NewGameEvent.class, () -> isEnabled, event -> {
            pendingMovements.clear();
            lastKnownPlayerPos.clear();
            awareness.clear();
            seenThisTick.clear();
            globalAlertness = GuardAlertness.RELAXED;
            alertTimer = 0;
        });

        // checks if a guard moves. if so, removes its pending movment to let it move to the next routed node
        bus.subscribe(MovementEvent.class, () -> isEnabled, event -> {
            var movementEvent = (MovementEvent) event;
            pendingMovements.remove(movementEvent.entityID);
        });

        // When VisionSystem emits a PlayerSeenEvent it means an emitter saw the
        // player during this tick. We increment the guard's awareness meter and
        // only escalate to a full PlayerSpottedEvent when threshold is reached.
        bus.subscribe(PlayerSeenEvent.class, () -> isEnabled, event -> {
            var seen = (PlayerSeenEvent) event;
            Logs.log("Player seen by guard " + seen.spotterId + " at " + seen.playerPosition);
            PositionComponent guardPosition = (PositionComponent) world.Entities.get(seen.spotterId).get(PositionComponent.class);
            GuardComponent guardComponent = (GuardComponent) world.Entities.get(seen.spotterId).get(GuardComponent.class);

            awareness.putIfAbsent(seen.spotterId, new HashMap<>());
            var perPlayer = awareness.get(seen.spotterId);
            int current = perPlayer.getOrDefault(seen.playerEntityId, 0);

            // adjust awareness relavtive to distance
            double distance = guardPosition.Origin.subtract(seen.playerPosition).magnitude();
            double distanceFactor = Math.max(0.1, 1 - distance / 100);

            // global alertness makes all guards gain awareness faster when the
            // world is on higher alert (e.g. someone has spotted the player).
            double globalFactor = 1.0;
            if (globalAlertness == GuardAlertness.SUSPICIOUS) globalFactor = 1.5;
            if (globalAlertness == GuardAlertness.ALERT) globalFactor = 3.0;

            // increase the awareness to 1 * the determined multipliers. cannot go > threshold
            // NOTE: awareness gain is now independent of the guard's behaviour state.
            current = (int) Math.min(AWARENESS_THRESHOLD, current + (int)Math.ceil(1 * distanceFactor * globalFactor));
            perPlayer.put(seen.playerEntityId, current);

            // mark that this guard saw this player this tick so we don't decay it
            seenThisTick.putIfAbsent(seen.spotterId, new java.util.HashSet<>());
            seenThisTick.get(seen.spotterId).add(seen.playerEntityId);

            // publish awareness change so UI can display meter
            bus.publish(new AwarenessChangedEvent(seen.spotterId, seen.playerEntityId, current, AWARENESS_THRESHOLD));

            if (current >= AWARENESS_THRESHOLD) {
                // escalate to a real spotted event and update last-known player position
                Logs.log("Guard " + seen.spotterId + " fully spotted player " + seen.playerEntityId);
                lastKnownPlayerPos.put(seen.spotterId, seen.playerPosition);
                bus.publish(new PlayerSpottedEvent(seen.spotterId, seen.playerEntityId, seen.playerPosition));
                // raise the global alertness to ALERT and start the persist timer
                alertTimer = ALERT_PERSIST_TICKS;
                if (globalAlertness != GuardAlertness.ALERT) {
                    globalAlertness = GuardAlertness.ALERT;
                    bus.publish(new GlobalAlertnessChangedEvent(globalAlertness));
                }
            }
        });

    }

    @Override
    public void onExit(World world) {
        super.onExit(world);
        if (newGameSubscription != null) {
            newGameSubscription.cancel.run();
            newGameSubscription = null;
        }
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

        // Decay awareness for guard/player pairs that were not seen in the most recent bus flush.
        // this runs at the start of the system update and ensures the meter falls when the
        // player is not being observed.
        updatePlayerAwarenessPerGuard();

        // Clear seenThisTick now so new sighting events (published during this
        // update) will be recorded into it on the next flush.
        seenThisTick.clear();

        updateGlobalPlayerAwareness();

        var guards = (HashSet<EntityID>) world.ComponentEntitiesIndex.query(new Class[] {GuardComponent.class, PositionComponent.class});
        for (var guardID : guards) {
            GuardComponent guardComp = (GuardComponent)world.Entities.get(guardID).get(GuardComponent.class);
            PositionComponent positionComp = (PositionComponent)world.Entities.get(guardID).get(PositionComponent.class);
            if (guardComp == null) continue;

            updateGuardRouting(world, guardID, guardComp);
            updateGuardSymbol(world, guardID);
        }
    }

    private void updateGlobalPlayerAwareness() {
        // determine overall max awareness across all guards so we can compute a global level
        if (alertTimer > 0) {
            alertTimer = Math.max(0, alertTimer - 1);
            if (globalAlertness != GuardAlertness.ALERT) {
                globalAlertness = GuardAlertness.ALERT;
                bus.publish(new GlobalAlertnessChangedEvent(globalAlertness));
            }
            // while alertTimer > 0 don't compute lower levels
        } else {
            GuardAlertness computedLevel = getGuardAlertness();

            if (computedLevel != globalAlertness) {
                globalAlertness = computedLevel;
                bus.publish(new GlobalAlertnessChangedEvent(globalAlertness));
            }
        }
    }

    private void updatePlayerAwarenessPerGuard() {
        if (!awareness.isEmpty()) {
            var guardIds = new ArrayList<>(awareness.keySet());
            for (var guardId : guardIds) {
                var perPlayer = awareness.get(guardId);
                var playerIds = new ArrayList<>(perPlayer.keySet());
                for (var playerId : playerIds) {
                    boolean seen = seenThisTick.containsKey(guardId) && seenThisTick.get(guardId).contains(playerId);
                    if (!seen) {
                        int cur = perPlayer.getOrDefault(playerId, 0);
                        int next = Math.max(0, cur - AWARENESS_DECAY_PER_TICK);
                        if (next != cur) {
                            perPlayer.put(playerId, next);
                            bus.publish(new AwarenessChangedEvent(guardId, playerId, next, AWARENESS_THRESHOLD));
                        }
                        if (next == 0) {
                            perPlayer.remove(playerId);
                        }
                    }
                }
                if (perPlayer.isEmpty()) {
                    awareness.remove(guardId);
                }
            }
        }
    }

    private void updateGuardRouting(World world, EntityID guardID, GuardComponent guardComp) {
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

    private static void updateGuardSymbol(World world, EntityID guardID) {
        RenderableComponent renderComp = (RenderableComponent) world.Entities.get(guardID).get(RenderableComponent.class);
        OrientationComponent orientationComp = (OrientationComponent) world.Entities.get(guardID).get(OrientationComponent.class);

        if (renderComp != null && orientationComp != null) {
            int angle = (orientationComp.facingAngle % 360 + 360) % 360;
            if (angle >= 315 || angle < 45) {
                renderComp.Char = '▶';
            } else if (angle >= 45 && angle < 135) {
                renderComp.Char = '▼';
            } else if (angle >= 135 && angle < 225) {
                renderComp.Char = '◀';
            } else {
                renderComp.Char = '▲';
            }
        }
    }

    private GuardAlertness getGuardAlertness() {
        int maxAcrossAll = 0;
        for (var g : awareness.values()) {
            for (var v : g.values()) {
                if (v > maxAcrossAll) maxAcrossAll = v;
            }
        }
        GuardAlertness computedLevel;
        if (maxAcrossAll == 0) computedLevel = GuardAlertness.RELAXED;
        else if (maxAcrossAll < AWARENESS_THRESHOLD) computedLevel = GuardAlertness.SUSPICIOUS;
        else computedLevel = GuardAlertness.ALERT;
        return computedLevel;
    }

    public void routeIfNecessary(GuardComponent guardComp, PositionComponent positionComp, Point targetPoint) {
        if (targetPoint == null) {
            return;
        }
        // if we don't yet have a path stack, or it is empty, create a new route
        if (guardComp.interpolatedPathPoints == null || guardComp.interpolatedPathPoints.isEmpty()) {
            guardComp.interpolatedPathPoints = new Stack<>();
            List<Point> nextPathSegment = routeBetween(positionComp.Origin, targetPoint);
            for (int i = nextPathSegment.size() - 1; i > 0; i--) {
                guardComp.interpolatedPathPoints.push(nextPathSegment.get(i));
            }
        }
    }
}
