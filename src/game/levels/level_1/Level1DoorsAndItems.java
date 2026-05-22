package game.levels.level_1;

import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.rendering.Colour;
import game.LevelContext;
import game.LevelSection;
import game.components.*;
import game.DoorSystem;

public class Level1DoorsAndItems implements LevelSection {
    @Override
    public void run(LevelContext context) {
        DoorSystem doorSystem = new DoorSystem(context.engine.Resources, context.engine.EventBus);
        var mainDoor = doorSystem.registerDoor(
                context.world,
                new Point(48, 18),
                new DoorComponent(new char[]{'['}, new char[]{' '}, 2, DoorOrientation.VERTICAL, true, true, true, "Staff Keycard")
        );
        context.world.addComponentToLayer(mainDoor, new MaskToTileMapComponent(context.playerVision));
        context.scene.add(doorSystem);

        context.world.createEntity(
                new PositionComponent(new Point(26, 17), 2),
                new RenderableComponent('k', new Colour(255, 215, 0), null, true),
                new ItemComponent("Staff Keycard", "A basic keycard used by employees - Someone seems to have dropped it?"),
                new MaskToTileMapComponent(context.playerVision)
        );
        context.world.createEntity(
                new PositionComponent(new Point(4, 3), 2),
                new RenderableComponent('u', new Colour(255, 215, 0), null, true),
                new ItemComponent("USB Stick", "A USB with 'confidential' scribbled in red marker. Seems important?"),
                new MaskToTileMapComponent(context.playerVision)
        );
    }
}
