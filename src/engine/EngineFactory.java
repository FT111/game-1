package engine;

/// Factory class for creating Engine instances with optional configuration
public class EngineFactory {
    World world = null;
    Renderer renderer = null;
    int ticksPerSecond = 60;

    public EngineFactory withWorld(World world) {
        this.world = world;
        return this;
    }

    public EngineFactory withRenderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public EngineFactory withTicksPerSecond(int ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        return this;
    }

    public Engine build() {
        try {
            Logs.log("EngineFactory.build: start (customWorld=" + (world != null) + ", customRenderer=" + (renderer != null) + ", ticksPerSecond=" + ticksPerSecond + ")");
            Engine engine = new Engine(world, renderer, ticksPerSecond);
            Logs.log("EngineFactory.build: success");
            return engine;
        } catch (Exception e) {
            Logs.log("EngineFactory.build: failed with " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new RuntimeException("Failed to create Engine instance", e);
        }
    }
}
