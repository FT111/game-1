package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.EventSubscriptionReceipt;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.OrientationComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.events.KeyInputEvent;
import engine_interfaces.objects.events.MouseInputEvent;
import engine_interfaces.objects.events.MovementProposalEvent;

import java.util.HashMap;

public class PlayerSystem extends System {
    private final HashMap<Character, Point> movementDirections = new HashMap<>() {{
        put('w', new Point(0, -1));
        put('a', new Point(-1, 0));
        put('s', new Point(0, 1));
        put('d', new Point(1, 0));
    }};

    private final EntityID playerEntity;
    private final EntityID cameraEntity;
    private PositionComponent cameraPosition;
    private Point cursorWorldPosition;

    private final EventBus bus;
    private EventSubscriptionReceipt mouseInputSubscription;
    private EventSubscriptionReceipt keyInputSubscription;


    public PlayerSystem(EventBus bus, EntityID playerEntity, EntityID cameraEntity) {
        this.playerEntity = playerEntity;
        this.cameraEntity = cameraEntity;
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        lockCameraToPlayer(world, cameraEntity);
        this.cameraPosition = (PositionComponent) world.Entities.get(cameraEntity).get(PositionComponent.class);

        mouseInputSubscription = bus.subscribe(MouseInputEvent.class, () -> isEnabled, event -> {
            var input = (MouseInputEvent) event;
//            if (input.eventType != MouseEventTypes.DRAG) {
//                return;
//            }

            this.cameraPosition = (PositionComponent) world.Entities.get(cameraEntity).get(PositionComponent.class);
            this.cursorWorldPosition = new Point(
                input.screenPosition.x() + cameraPosition.Origin.x(),
                input.screenPosition.y() + cameraPosition.Origin.y()
            );
            var orientation = (OrientationComponent) world.Entities.get(playerEntity).get(OrientationComponent.class);
            var playerPosition = getPlayerWorldPosition(world);
            // get the angle of the mouse cursor relative to the player
            // the y-axis is multiplied by two to account for grid cells being ~2x taller than they are wide
            orientation.facingAngle = (int) Math.toDegrees(Math.atan2((cursorWorldPosition.y() - playerPosition.Origin.y())*2, cursorWorldPosition.x() - playerPosition.Origin.x()));
        });

        keyInputSubscription = bus.subscribe(KeyInputEvent.class, () -> isEnabled, event -> {
            var input = (KeyInputEvent) event;

            if (movementDirections.containsKey(input.key)) {
                Point direction = movementDirections.get(input.key);
                CameraComponent cameraDetails = (CameraComponent) world.Entities.get(cameraEntity).get(CameraComponent.class);
                PositionComponent cameraPosition = (PositionComponent) world.Entities.get(cameraEntity).get(PositionComponent.class);
                var proposedPlayerPosition = getPlayerWorldPosition(world).Origin.add(direction);

                // propose player movement
                var playerMovementProposal = bus.publish(new MovementProposalEvent(playerEntity, proposedPlayerPosition, getPlayerWorldPosition(world).Origin));
                // calculate and propose camera movement in the same direction as the player movement, but only if the player is outside a 4x4 deadzone around the center of the camera
                if (Math.abs(proposedPlayerPosition.x() - (cameraPosition.Origin.x() + cameraDetails.viewWidth / 2)) <= 2 && Math.abs(proposedPlayerPosition.y() - (cameraPosition.Origin.y() + cameraDetails.viewHeight / 2)) <= 2) {
                    return;
                }

                bus.publish(new MovementProposalEvent(cameraEntity, new Point(cameraPosition.Origin.x() + direction.x(), cameraPosition.Origin.y() + direction.y()), cameraPosition.Origin,
                        playerMovementProposal.eventID));
            }
        });
    }

    @Override
    public void onExit(World world) {
        if (mouseInputSubscription != null) {
            mouseInputSubscription.cancel.run();
            mouseInputSubscription = null;
        }

        if (keyInputSubscription != null) {
            keyInputSubscription.cancel.run();
            keyInputSubscription = null;
        }
    }

    private void moveInDirection(World world, Point direction) {
        var playerPosition = getPlayerWorldPosition(world);

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

    public PositionComponent getPlayerWorldPosition(World world) {
        return (PositionComponent) world.Entities.get(playerEntity).get(PositionComponent.class);
    }

    public void lockCameraToPlayer(World world, EntityID cameraEntity) {
        var newPosition = getPlayerWorldPosition(world).Origin;
        CameraComponent cameraDetails = (CameraComponent) world.Entities.get(cameraEntity).get(CameraComponent.class);


        world.Entities.get(cameraEntity).put(PositionComponent.class, new PositionComponent(
                new Point(newPosition.x() - cameraDetails.viewWidth / 2, newPosition.y() - cameraDetails.viewHeight / 2)
        ));
    }


    @Override
    public void update(World world, int tickCount) {
        // IO.println("Player position: " + getPlayerWorldPosition(world).Origin);
    }
}
