package engine_interfaces.objects;

import engine.World;

public abstract class System {
    public boolean isEnabled = true;

    public abstract void update(World world, int tickCount);
}
