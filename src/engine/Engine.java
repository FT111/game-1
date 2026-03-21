package engine;

import engine.rendering.EntityRenderPass;
import engine.rendering.TileMapRenderPass;
import engine.systems.MovementSystem;
import engine.systems.InputHandlerSystem;
import engine.systems.movement.CollisionProcessor;
import engine.systems.movement.VelocityProcessor;
import engine_interfaces.objects.rendering.RenderPass;

import java.io.IOException;
import java.util.ArrayList;

public class Engine {
    public World World;
    public Renderer Renderer;
    public Systems Systems = new Systems();
    public Resources Resources = new Resources();
    public EventBus EventBus = new EventBus();

    // Turn these into a config class later
    public int TicksPerSecond = 60;
    private ArrayList<RenderPass> CoreRenderPasses = new ArrayList<>() {{
        add(new TileMapRenderPass());
        add(new EntityRenderPass());
    }};
    // ------------------------------------

    public Engine(World world, Renderer renderer, int ticksPerSecond) throws IOException {
        World = (world != null) ? world : new World(EventBus);
        Renderer = (renderer != null) ? renderer : new Renderer(new LanternaAPI());
        TicksPerSecond = ticksPerSecond;

        // Add default render passes
        Renderer.renderPasses.addAll(CoreRenderPasses);

        // Add input system
        Systems.addSystem(new InputHandlerSystem(Renderer.Api, EventBus));

        MovementSystem movementSys = new MovementSystem(EventBus);
        movementSys.movementPipeline.add(new CollisionProcessor(EventBus, World, Resources));
        movementSys.movementPipeline.add(new VelocityProcessor());
        Systems.addSystem(movementSys);
    }

    public void StartGameLoop() {
        int tick = 0;

        while (Thread.currentThread().isAlive()) {
            var CurrentTime = System.nanoTime();

            this.EventBus.flush();

            Systems.update(World, tick);

            try {
                Renderer.render(World, Resources);
             } catch (IOException e) {
                e.printStackTrace();
            }

            delay(CurrentTime);
            tick++;
        }
    }

    private void delay(long CurrentTime) {
        long targetTime = 1_000_000_000 / TicksPerSecond;
        long elapsedTime = System.nanoTime() - CurrentTime;
        long sleepTime = targetTime - elapsedTime;

        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
