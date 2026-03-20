package resources;

import engine.EventBus;
import engine.Systems;
import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
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


    public PlayerSystem(EventBus bus, World world, EntityID playerEntity) {

        bus.subscribe(InputEvent.class, "PlayerSystem", event -> {
            var input = (InputEvent) event;

            if (movementDirections.containsKey(input.key)) {
                var playerPosition = (PositionComponent) world.Entities.get(playerEntity).get(PositionComponent.class);
                Point direction = movementDirections.get(input.key);
                Point newPosition = playerPosition.Origin.add(direction);

                world.Entities.get(playerEntity).put(PositionComponent.class, new PositionComponent(
                        new Point(newPosition.x(), newPosition.y())
                ));
            }
         });
    }

    @Override
    public void update(World world) {

    }
}
