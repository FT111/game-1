package resources;

import engine.World;
import engine_interfaces.objects.System;
import resources.components.GuardComponent;

public class GuardAiSystem extends System {
    @Override
    public void onEnter(World world) {
        super.onEnter(world);

    }

    @Override
    public void update(World world, int tickCount) {
        super.update(world, tickCount);

        var guards = world.ComponentEntitiesIndex.query(GuardComponent.class);
    }
}
