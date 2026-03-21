package resources;

import engine.EventBus;
import engine.Systems;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.events.InputEvent;

import java.util.HashMap;

public class PlayerSystem extends System {
    private final HashMap<Character, Point> movementDirections = new HashMap<>() {{
        put('w', new Point(0, -1));
        put('a', new Point(-1, 0));
        put('s', new Point(0, 1));
        put('d', new Point(1, 0));
    }};


    public PlayerSystem(EventBus bus, World world, EntityID playerEntity, EntityID cameraEntity) {
        lockCameraToPlayer(world, cameraEntity, ((PositionComponent) world.Entities.get(playerEntity).get(PositionComponent.class)).Origin);

        bus.subscribe(InputEvent.class, "PlayerSystem", event -> {
            var input = (InputEvent) event;

            if (movementDirections.containsKey(input.key)) {
                var playerPosition = (PositionComponent) world.Entities.get(playerEntity).get(PositionComponent.class);
                Point direction = movementDirections.get(input.key);
                Point newPosition = playerPosition.Origin.add(direction);


                world.Entities.get(playerEntity).put(PositionComponent.class, new PositionComponent(
                        new Point(newPosition.x(), newPosition.y())
                ));
                CameraComponent cameraDetails = (CameraComponent) world.Entities.get(cameraEntity).get(CameraComponent.class);
                PositionComponent cameraPosition = (PositionComponent) world.Entities.get(cameraEntity).get(PositionComponent.class);

                // first check if player is with a 4x4 deadzone around the center of the camera, if so, don't move the camera
                if (Math.abs(newPosition.x() - (cameraPosition.Origin.x() + cameraDetails.viewWidth / 2)) <= 2 && Math.abs(newPosition.y() - (cameraPosition.Origin.y() + cameraDetails.viewHeight / 2)) <= 2) {
                    return;
                }
                // move the camera by the same amount as the player movement
                world.Entities.get(cameraEntity).put(PositionComponent.class, new PositionComponent(
                        new Point(cameraPosition.Origin.x() + direction.x(), cameraPosition.Origin.y() + direction.y())
                ));
            }
         });
    }

    private static void lockCameraToPlayer(World world, EntityID cameraEntity, Point newPosition) {
        CameraComponent cameraDetails = (CameraComponent) world.Entities.get(cameraEntity).get(CameraComponent.class);


        world.Entities.get(cameraEntity).put(PositionComponent.class, new PositionComponent(
                new Point(newPosition.x() - cameraDetails.viewWidth / 2, newPosition.y() - cameraDetails.viewHeight / 2)
        ));
    }


    @Override
    public void update(World world) {

    }
}
