package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.events.CollisionEvent;
import engine_interfaces.objects.events.InputEvent;
import engine_interfaces.objects.events.MovementProposalEvent;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PlayerSystem extends System {
    private final HashMap<Character, Point> movementDirections = new HashMap<>() {{
        put('w', new Point(0, -1));
        put('a', new Point(-1, 0));
        put('s', new Point(0, 1));
        put('d', new Point(1, 0));
    }};

    private final EntityID playerEntity;
    private final EntityID cameraEntity;

    private EventBus bus;


    public PlayerSystem(EventBus bus, World world, EntityID playerEntity, EntityID cameraEntity) {
        this.playerEntity = playerEntity;
        this.cameraEntity = cameraEntity;
        this.bus = bus;
        lockCameraToPlayer(world, cameraEntity);


//        bus.subscribe(CollisionEvent.class, "PlayerSystem", event ->
//        {
//            var collision = (CollisionEvent) event;
//            if (collision.entityId == playerEntity) QueuedMovements.clear();
//        });

        bus.subscribe(InputEvent.class, "PlayerSystem", event -> {
            var input = (InputEvent) event;

            if (movementDirections.containsKey(input.key)) {
                Point direction = movementDirections.get(input.key);
                CameraComponent cameraDetails = (CameraComponent) world.Entities.get(cameraEntity).get(CameraComponent.class);
                PositionComponent cameraPosition = (PositionComponent) world.Entities.get(cameraEntity).get(PositionComponent.class);
                var proposedPlayerPosition = getPlayerPosition(world).Origin.add(direction);

                // propose player movement
                var playerMovementProposal = bus.publish(new MovementProposalEvent(playerEntity, proposedPlayerPosition, getPlayerPosition(world).Origin));
                // calculate and propose camera movement in the same direction as the player movement, but only if the player is outside a 4x4 deadzone around the center of the camera
                if (Math.abs(proposedPlayerPosition.x() - (cameraPosition.Origin.x() + cameraDetails.viewWidth / 2)) <= 2 && Math.abs(proposedPlayerPosition.y() - (cameraPosition.Origin.y() + cameraDetails.viewHeight / 2)) <= 2) {
                    return;
                }

                bus.publish(new MovementProposalEvent(cameraEntity, new Point(cameraPosition.Origin.x() + direction.x(), cameraPosition.Origin.y() + direction.y()), cameraPosition.Origin,
                        playerMovementProposal.eventID));
            }
        });
    }

    private void moveInDirection(World world, Point direction) {
        var playerPosition = getPlayerPosition(world);

        Point newPosition = playerPosition.Origin.add(direction);


        world.Entities.get(playerEntity).put(PositionComponent.class, new PositionComponent(
                new Point(newPosition.x(), newPosition.y())
        ));
//        CameraComponent cameraDetails = (CameraComponent) world.Entities.get(cameraEntity).get(CameraComponent.class);
//        PositionComponent cameraPosition = getPlayerPosition(world);
//
//        // first check if player is with a 4x4 deadzone around the center of the camera, if so, don't move the camera
//        if (Math.abs(newPosition.x() - (cameraPosition.Origin.x() + cameraDetails.viewWidth / 2)) <= 2 && Math.abs(newPosition.y() - (cameraPosition.Origin.y() + cameraDetails.viewHeight / 2)) <= 2) {
//            return;
//        }
//        // move the camera by the same amount as the player movement
//        world.Entities.get(cameraEntity).put(PositionComponent.class, new PositionComponent(
//                new Point(cameraPosition.Origin.x() + direction.x(), cameraPosition.Origin.y() + direction.y())
//        ));
    }

    public PositionComponent getPlayerPosition(World world) {
        return (PositionComponent) world.Entities.get(playerEntity).get(PositionComponent.class);
    }

    public void lockCameraToPlayer(World world, EntityID cameraEntity) {
        var newPosition = getPlayerPosition(world).Origin;
        CameraComponent cameraDetails = (CameraComponent) world.Entities.get(cameraEntity).get(CameraComponent.class);


        world.Entities.get(cameraEntity).put(PositionComponent.class, new PositionComponent(
                new Point(newPosition.x() - cameraDetails.viewWidth / 2, newPosition.y() - cameraDetails.viewHeight / 2)
        ));
    }


    @Override
    public void update(World world, int tickCount) {
        IO.println("Player position: " + getPlayerPosition(world).Origin);
    }
}
