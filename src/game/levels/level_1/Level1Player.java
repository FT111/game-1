package game.levels.level_1;

import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.rendering.Colour;
import game.LevelContext;
import game.LevelSection;
import game.components.*;

public class Level1Player implements LevelSection {
    @Override
    public void run(LevelContext context) {
        var player = context.world.createEntity(
                new PositionComponent(new Point(32,22), 3),
                new RenderableComponent('@', null, null, true),
                new VelocityComponent(1.2, 8,  "exponential"),
                new VisionEmitterComponent(150, 110, 5, context.playerVision, new Colour(0, 40, 40)),
                new PlayerComponent(),
                new OrientationComponent(90),
                new InventoryComponent()
        );

        context.world.addComponentToLayer(context.playerVision, new VisionLayerComponent(player));
        context.world.addComponentToLayer(context.playerVision, new TileMapComponent("vision-maps", player.toString(), "tl", false));
    }
}
