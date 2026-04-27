package engine_interfaces.objects;

import engine.World;
import engine.scenes.Scene;

public abstract class System {
    public boolean isEnabled = true;

    public void onEnter(World world) {}

    public void onExit(World world) {}

    public void afterSceneChange(Scene fromScene, Scene toScene, World world) {}

    public void update(World world, int tickCount) {}
}
