package engine;

import engine.rendering.EntityRenderPass;
import engine.rendering.TextRenderPass;
import engine.rendering.TileMapRenderPass;
import engine.rendering.BlockRenderPass;
import engine.systems.MovementSystem;
import engine.systems.InputHandlerSystem;
import engine.systems.SceneGraphSystem;
import engine.systems.UiInteractionSystem;
import engine.systems.movement.CollisionProcessor;
import engine.systems.movement.VelocityProcessor;
import engine.scenes.SceneManager;
import engine.layout.LayoutManager;
import engine_interfaces.objects.components.LayerColliderComponent;
import engine_interfaces.objects.components.ui.ClickComponent;
import engine_interfaces.objects.components.ui.HoverComponent;
import engine_interfaces.objects.rendering.RenderPass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Engine {
    public World World;
    public Renderer Renderer;
    public Systems Systems = new Systems();
    public Resources Resources = new Resources();
    public EventBus EventBus = new EventBus();
    public SceneManager SceneManager;
    public LayoutManager LayoutManager;

    private long Accumulator = 0;
    public final List<Class<? extends engine_interfaces.objects.System>> CoreSystems = new ArrayList<>() {{
        add(InputHandlerSystem.class);
        add(MovementSystem.class);
        add(SceneGraphSystem.class);
        add(UiInteractionSystem.class);
    }};

    // Turn these into a config class later
    public int TicksPerSecond = 60;
    public int FrameRateLimit = 120;
        private ArrayList<RenderPass> CoreRenderPasses = new ArrayList<>() {{
        add(new TileMapRenderPass());
        add(new BlockRenderPass());
        add(new EntityRenderPass());
        add(new TextRenderPass());
    }};
    // ------------------------------------

    public Engine(World world, Renderer renderer, int ticksPerSecond) throws IOException, InterruptedException {
        Logs.log("Engine: constructor start");
        World = (world != null) ? world : new World(EventBus);
        Logs.log("Engine: world ready (custom=" + (world != null) + ")");
        LayoutManager = new LayoutManager(World, EventBus, Resources);

        LayoutManager.defineMapLayer("collision")
                .fromTileMap(LayerColliderComponent.class, c -> c.collidableTiles)
                .register();

        LayoutManager.defineMapLayer("clickable")
                .fromBoundingBox(ClickComponent.class)
                .register();

        LayoutManager.defineMapLayer("hoverable")
                .fromBoundingBox(HoverComponent.class)
                .register();

        Renderer = (renderer != null) ? renderer : new Renderer(new LanternaAPI(), LayoutManager);
        Logs.log("Engine: renderer ready (custom=" + (renderer != null) + ", api=" + Renderer.Api.getClass().getSimpleName() + ")");
        SceneManager = new SceneManager(EventBus, World, Systems);
        Logs.log("Engine: scene manager ready");
        TicksPerSecond = ticksPerSecond;
        Logs.log("Engine: tick rate set to " + TicksPerSecond);

        // Add default render passes
        Renderer.renderPasses.addAll(CoreRenderPasses);
        Logs.log("Engine: added core render passes (count=" + CoreRenderPasses.size() + ")");

        // Add input system
        Systems.addSystem(new InputHandlerSystem(Renderer.Api, EventBus));
        Logs.log("Engine: input handler system added");


        Systems.addSystem(new SceneGraphSystem(EventBus));
        Logs.log("Engine: scene graph system added");
        Logs.log("Engine: constructor complete");
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
                LayoutManager.invalidate();
                Systems.update(World, tick);

                Accumulator -= 1_000_000_000 / TicksPerSecond;
            }

            // Decoupled rendering from tick updates
            try {
                LayoutManager.invalidate();
                Renderer.render(World, Resources);

                // Sleep if we're ahead of the frame rate limit
                long frameTime = System.nanoTime() - currentTime;
                long targetFrameTime = 1_000_000_000 / FrameRateLimit;
                if (frameTime < targetFrameTime) {
                    Thread.sleep((targetFrameTime - frameTime) / 1_000_000, (int) ((targetFrameTime - frameTime) % 1_000_000));
                }
            } catch (Exception e) {
                Logs.log("Engine: rendering failed - " + e.getMessage());

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
