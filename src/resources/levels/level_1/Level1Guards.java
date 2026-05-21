package resources.levels.level_1;

import engine_interfaces.objects.Point;
import resources.LevelContext;
import resources.LevelSection;
import resources.factories.GuardFactory;

import java.util.List;

public class Level1Guards implements LevelSection {
    @Override
    public void run(LevelContext context) {
        var guardFactory = new GuardFactory(context.world, context.engine.Resources, "vision-maps", context.playerVision);
        guardFactory.create(new Point(15,10), List.of(new Point(17, 20), new Point(52,20), new Point(30, 8)));
    }
}
