package engine;

/// Factory class for creating Engine instances with optional configuration
public class EngineFactory {
    World world = null;
    Renderer renderer = null;

    public EngineFactory withWorld(World world) {
        this.world = world;
        return this;
    }

    public EngineFactory withRenderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Engine build() {
        return new Engine(world, renderer);
    }
}
