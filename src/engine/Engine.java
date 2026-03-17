package engine;

public class Engine {
    public World World;
    public Renderer Renderer;

    public Engine(World world, Renderer renderer) {
        World = (world != null) ? world : new World();
        Renderer = (renderer != null) ? renderer : new Renderer(new LanternaAPI());
    }
}
