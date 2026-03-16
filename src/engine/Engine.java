package engine;

public class Engine {
    public World World;
        public Renderer Renderer;

        public Engine() {
            World = new World();
            Renderer = new Renderer();
        }
}
