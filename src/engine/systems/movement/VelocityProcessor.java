package engine.systems.movement;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.MovementProcessor;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.VelocityComponent;
import engine_interfaces.objects.events.MovementProposalEvent;

import java.util.HashMap;

public class VelocityProcessor implements MovementProcessor {

    @Override
    public boolean validateMove(HashMap<EntityID, HashMap<Class<? extends Component>, Component>> entityState, int tickCount, MovementProposalEvent proposal) {
        if (entityState.get(proposal.entityID).getOrDefault(VelocityComponent.class, null) == null) {
            return true; // no velocity component means no movement restrictions
        }


        var positionComponent = (PositionComponent) entityState.get(proposal.entityID).get(PositionComponent.class);
        var velocityComponent = (VelocityComponent) entityState.get(proposal.entityID).get(VelocityComponent.class);

        if (velocityComponent.lastMovementTick == 0) {
            velocityComponent.lastMovementTick = tickCount; // Initialize last movement tick
            return true; // first move is always valid
        }


        if ((tickCount - velocityComponent.lastMovementTick) <= velocityComponent.maxMovementFrequency) {
            return false; // too soon to move again
        }

        switch (velocityComponent.accelerationFunction) {
            case "linear" -> {
                break;
            }
            case "exponential" -> {
                int ticksSinceLastMove = tickCount - velocityComponent.lastMovementTick;
                int currentMaxMovementFrequency = (int) Math.max(velocityComponent.baseMovementFrequency * Math.pow(velocityComponent.accelerationScaleConstant, ticksSinceLastMove), velocityComponent.maxMovementFrequency);

                if (ticksSinceLastMove < currentMaxMovementFrequency) {
                    IO.println("blocking due to acceration: " + ticksSinceLastMove + " < " + currentMaxMovementFrequency);
                    return false; // Not enough time has passed for the next move
                }
            }
        }


        velocityComponent.lastMovementTick = tickCount; // Update last movement tick
        return true;
    }
}
