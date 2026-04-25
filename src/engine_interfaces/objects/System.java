package engine_interfaces.objects;

import engine.World;
import engine.scenes.Scene;

public abstract class System {
    public boolean isEnabled = true;

    public void onEnter(World world) {}

    public void onExit(World world) {}

    public void onSceneChange(Scene fromScene, Scene toScene, World world) {}

    public abstract void update(World world, int tickCount);
}
