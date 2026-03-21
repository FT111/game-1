package engine.systems;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.MovementProcessor;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.events.MovementProposalEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

public class MovementSystem extends System {
    private final ConcurrentLinkedDeque<MovementProposalEvent> pendingMovements;

    public ArrayList<MovementProcessor> movementPipeline;

    public MovementSystem(EventBus Bus) {
        pendingMovements = new ConcurrentLinkedDeque<>();
        movementPipeline = new ArrayList<>();


        // Accumulate movement proposals
        Bus.subscribe(MovementProposalEvent.class, "MovementSystem",
        event -> {
            var movementProposal = (MovementProposalEvent) event;
            pendingMovements.add(movementProposal);
        });
    }


    @Override
    public void update(World world, int tickCount) {
        // stores all components so that the state can be atomically updated after processing all movement proposals
        var entityStateSnapshot = (HashMap<EntityID, HashMap<Class<? extends Component>, Component>>) world.Entities.clone();

        while (!pendingMovements.isEmpty()) {
            var movementProposal = pendingMovements.poll();

            boolean shouldMove = movementPipeline.stream().allMatch(processor -> processor.validateMove(movementProposal));

            if (shouldMove) {
                var positionComponent = (PositionComponent) entityStateSnapshot.get(movementProposal.entityID).get(PositionComponent.class);
                positionComponent.Origin = movementProposal.proposedPosition;
            }
        }

        world.Entities = entityStateSnapshot;
    }
}
