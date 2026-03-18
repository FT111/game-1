package engine;

import engine_interfaces.objects.System;

import java.util.HashMap;
import java.util.HashSet;

public class Systems {
    private final HashMap<Class<? extends System>, System> systems = new HashMap<>();

    public void addSystem(System system) {
        systems.put(system.getClass(), system);
    }

    protected void update(World world) {
        systems.forEach((systemClass, system) -> system.update(world));
    }
}
