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
            return new Engine(world, renderer, ticksPerSecond);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Engine instance", e);
        }
    }
}
