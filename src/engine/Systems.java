package engine;

import engine_interfaces.objects.System;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Systems {
    private final HashMap<Class<? extends System>, System> systems = new HashMap<>();
    private final LinkedList<Class<? extends System>> systemOrder = new LinkedList<>();

    public void addSystem(System system) {
        systems.put(system.getClass(), system);
        systemOrder.add(system.getClass());
    }

    public void addSystem(System system, Class<? extends System> after) {
        systems.put(system.getClass(), system);
        int index = systemOrder.indexOf(after);
        if (index == -1) {
            throw new IllegalArgumentException("System " + after.getName() + " not found");
        }
        systemOrder.add(index + 1, system.getClass());
    }

    public void removeSystem(Class<? extends System> systemClass) {
        systems.remove(systemClass);
        systemOrder.remove(systemClass);
    }

    public System getSystem(Class<? extends System> systemClass) {
        return systems.get(systemClass);
    }

    protected void update(World world, int tickCount) {
        systemOrder.forEach(systemClass -> systems.get(systemClass).update(world, tickCount));
    }
}
