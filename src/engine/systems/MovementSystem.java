package engine.systems;

import engine.CoreWorld;
import engine.EventBus;
import engine.Logs;
import engine.World;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.EventSubscriptionReceipt;
import engine_interfaces.objects.MovementProcessor;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.OrientationComponent;
import engine_interfaces.objects.components.OrientToMovementComponent;
import engine_interfaces.objects.events.MovementEvent;
import engine_interfaces.objects.events.MovementProposalEvent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MovementSystem extends System {
    private final ConcurrentLinkedDeque<MovementProposalEvent> pendingMovements;
    private final HashSet<UUID> approvedMovementEventsThisTick = new HashSet<>();
    private final EventBus bus;
    private EventSubscriptionReceipt movementProposalSubscription;

    public ArrayList<MovementProcessor> movementPipeline;

    public MovementSystem(EventBus bus) {
        pendingMovements = new ConcurrentLinkedDeque<>();
        movementPipeline = new ArrayList<>();
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        movementProposalSubscription = bus.subscribe(MovementProposalEvent.class, () -> isEnabled,
            event -> {
                if (!(event instanceof MovementProposalEvent movementProposal)) {
                    return;
                }
                pendingMovements.add(movementProposal);
            });
    }

    @Override
    public void onExit(World world) {
        if (movementProposalSubscription != null) {
            movementProposalSubscription.cancel.run();
            movementProposalSubscription = null;
        }

        pendingMovements.clear();
        approvedMovementEventsThisTick.clear();
    }


    @Override
    public void update(World world, int tickCount) {
        // stores all components so that the state can be atomically updated after processing all movement proposals
        var entityStateSnapshot = new HashMap<>(world.Entities);

        List<MovementProposalEvent> toRepropose = new ArrayList<>();
        while (!pendingMovements.isEmpty()) {
            var movementProposal = pendingMovements.poll();

            boolean shouldMove = movementPipeline.stream().allMatch(processor -> processor.validateMove(entityStateSnapshot, tickCount, movementProposal));
            if (movementProposal.dependsOnMovement != null) {
                shouldMove = shouldMove && approvedMovementEventsThisTick.contains(movementProposal.dependsOnMovement);
            }

            if (shouldMove) {
                var positionComponent = CoreWorld.getComponent(entityStateSnapshot.get(movementProposal.entityID), PositionComponent.class);
                positionComponent.Origin = movementProposal.proposedPosition;

                // Update facing based on movement when requested by a marker component.
                if (entityStateSnapshot.get(movementProposal.entityID).containsKey(OrientToMovementComponent.class)) {
                    int dx = movementProposal.proposedPosition.x() - movementProposal.currentPosition.x();
                    int dy = movementProposal.proposedPosition.y() - movementProposal.currentPosition.y();
                    int facingAngle = (int) Math.toDegrees(Math.atan2(dy * 2.0, dx));
                    var orientation = CoreWorld.getComponent(entityStateSnapshot.get(movementProposal.entityID), OrientationComponent.class);
                    if (orientation == null) {
                        orientation = new OrientationComponent(facingAngle);
                        entityStateSnapshot.get(movementProposal.entityID).put(OrientationComponent.class, orientation);
                    } else {
                        orientation.facingAngle = facingAngle;
                    }
                }

                approvedMovementEventsThisTick.add(movementProposal.eventID);
                bus.publish(new MovementEvent(movementProposal.entityID, movementProposal.currentPosition, movementProposal.proposedPosition));
                continue;
            }
            if (movementProposal.retryOnRejection) {
                toRepropose.add(movementProposal);
            }
        }
        pendingMovements.addAll(toRepropose);

        world.Entities = entityStateSnapshot;
    }
}
