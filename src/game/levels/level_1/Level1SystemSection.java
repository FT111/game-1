package game.levels.level_1;

import engine.systems.MovementSystem;
import engine.systems.movement.CollisionProcessor;
import engine.systems.movement.VelocityProcessor;
import game.*;

public class Level1SystemSection implements LevelSection {
    @Override
    public void run(LevelContext context) {
        context.scene.add(new ChunkSystem(context.engine.EventBus, context.engine.World, 8, context.chunkMap));
        context.scene.add(new PlayerSystem(context.engine.EventBus));
        context.scene.add(new FogSystem(context.engine.Resources));
        context.scene.add(new GameEndSystem(context.engine));

        MovementSystem movementSys = new MovementSystem(context.engine.EventBus);
        movementSys.movementPipeline.add(new CollisionProcessor(context.engine.EventBus, context.engine.World, context.engine.Resources, context.engine.LayoutManager));
        movementSys.movementPipeline.add(new VelocityProcessor());
        context.scene.add(movementSys);

        context.scene.add(new VisionSystem(context.engine.World, context.engine.Resources, context.engine.EventBus));
        context.scene.add(new GuardAiSystem(context.engine.LayoutManager, new game.movement.pathfinders.AStarPathfinder(), context.engine.EventBus));
        context.scene.add(new InventorySystem(context.engine.EventBus, context.engine.Resources));
    }
}
