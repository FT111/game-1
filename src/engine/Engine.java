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

    private long Accumulator = 0;

    // Turn these into a config class later
    public int TicksPerSecond = 60;
    public int FrameRateLimit = 240;
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

    public void ShowOutput() throws IOException {
        Renderer.Api.showWindow();
    }

    public void HideOutput() throws IOException {
        Renderer.Api.hideWindow();
    }

    public void StartGameLoop() {
        int tick = 0;
        var previousTime = System.nanoTime();
        long currentTime;

        while (Thread.currentThread().isAlive()) {
            currentTime = System.nanoTime();
            Accumulator += currentTime - previousTime;
            previousTime = currentTime;

            // Update game state based on fixed tick rate
            while (Accumulator >= 1_000_000_000 / TicksPerSecond) {
                this.EventBus.flush();
                Systems.update(World, tick);

                Accumulator -= 1_000_000_000 / TicksPerSecond;
            }

            // Decoupled rendering from tick updates
            try {
                Renderer.render(World, Resources);

                // Sleep if we're ahead of the frame rate limit
                long frameTime = System.nanoTime() - currentTime;
                long targetFrameTime = 1_000_000_000 / FrameRateLimit;
                if (frameTime < targetFrameTime) {
                    Thread.sleep((targetFrameTime - frameTime) / 1_000_000, (int) ((targetFrameTime - frameTime) % 1_000_000));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


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
