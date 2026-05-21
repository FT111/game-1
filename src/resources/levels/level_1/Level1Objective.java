package resources.levels.level_1;

import engine_interfaces.objects.Point;
import resources.LevelContext;
import resources.LevelSection;
import resources.objectives.*;
import resources.objectives.rules.AndRule;
import resources.objectives.rules.OrRule;
import resources.objectives.rules.HasItemRule;
import resources.objectives.rules.ReachLocationRule;

public class Level1Objective implements LevelSection {
    @Override
    public void run(LevelContext context) {
        ObjectiveManager objectiveManager = new ObjectiveManager();
        HasItemRule hasItem = new HasItemRule("USB Stick", "Find the USB stick with compromising material");
        ReachLocationRule escapeOpOne = new ReachLocationRule(new Point(33, 21), 1, "Escape via the main lift");
        ReachLocationRule escapeOpTwo = new ReachLocationRule(new Point(6, 20 ), 1, "Find and escape via the service lift" );
        OrRule escape = new OrRule(escapeOpOne, escapeOpTwo);
        Objective findUSB = new Objective("Find the USB",hasItem);
        Objective escapeObjective = new Objective("Escape", new AndRule(escape, hasItem, "Escape"));

        objectiveManager.addObjective(findUSB);
        objectiveManager.addObjective(escapeObjective);

        context.scene.add(new ObjectiveSystem(objectiveManager, context.engine.EventBus));
    }
}
