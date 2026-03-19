package engine;

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
        var TimeTaken = System.nanoTime() - CurrentTime;
        var TimeToSleep = (1000000000 / TicksPerSecond) - TimeTaken;
        if (TimeToSleep > 0) {
            try {
                Thread.sleep(TimeToSleep / 1000000, (int)(TimeToSleep % 1000000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
