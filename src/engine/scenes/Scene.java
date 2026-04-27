package engine.scenes;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.System;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class Scene {
    public final Set<System> systems = new HashSet<>();
    public final SceneWorld world = new SceneWorld();

    private Runnable onEnterCallback;
    private Runnable onExitCallback;
    public boolean isActive = false;

    public Scene() {}

    public Scene add(System system) {
        systems.add(system);
        return this;
    }

    public Scene setOnEnter(Runnable onEnter) {
        this.onEnterCallback = onEnter;
        return this;
    }

    public Scene setOnExit(Runnable onExit) {
        this.onExitCallback = onExit;
        return this;
    }

    public Set<System> getSystems() {
        return systems;
    }
    public Set<EntityID> getEntities() {return world.Entities.keySet();}
    public Set<LayerID> getLayers() {return world.Layers.keySet();}

    public void enter() {
        isActive = true;

        if (onEnterCallback != null) onEnterCallback.run();
        onEnter();
    }

    public void exit() {
        isActive = false;
        if (onExitCallback != null) onExitCallback.run();
        onExit();
    }

    protected void onEnter() {}

    protected void onExit() {}
}
