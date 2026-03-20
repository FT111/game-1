package engine;

import engine.rendering.TileMapRenderPass;

import java.io.IOException;

public class Engine {
    public World World;
    public Renderer Renderer;
    public Systems Systems = new Systems();
    public Resources Resources = new Resources();
    public int TicksPerSecond = 60;

    public Engine(World world, Renderer renderer, int ticksPerSecond) throws IOException {
        World = (world != null) ? world : new World();
        Renderer = (renderer != null) ? renderer : new Renderer(new LanternaAPI());
        TicksPerSecond = ticksPerSecond;

        this.Renderer.renderPasses
                .add(new TileMapRenderPass());
    }

    public void StartGameLoop() {
        while (Thread.currentThread().isAlive()) {
            var CurrentTime = System.nanoTime();

            Systems.update(World);

            try {
                Renderer.render(World, Resources);
             } catch (IOException e) {
                e.printStackTrace();
            }

            delay(CurrentTime);
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
