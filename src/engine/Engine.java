package engine;

import java.io.IOException;

public class Engine {
    public World World;
    public Renderer Renderer;

    public Engine(World world, Renderer renderer) throws IOException {
        World = (world != null) ? world : new World();
        Renderer = (renderer != null) ? renderer : new Renderer(new LanternaAPI());
    }
}
