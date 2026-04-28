package resources;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.rendering.GraphicsAPI;

public class CameraSystem extends System {
    private final EntityID level;
    private final GraphicsAPI api;
    private Runnable resizeSubscriptionCancel;
    private World activeWorld;

    public CameraSystem(EntityID level, GraphicsAPI api, World world) {
        this.level = level;
        this.api = api;
        this.activeWorld = world;
    }

    @Override
    public void onEnter(World world) {
        activeWorld = world;
        resizeSubscriptionCancel = api.onResize(() -> {
            CameraComponent camera = (CameraComponent) activeWorld.Entities.get(level).get(CameraComponent.class);
            camera.viewWidth = api.getWidth();
            camera.viewHeight = api.getHeight();
            // IO.println("Resized to: " + api.getWidth() + "x" + api.getHeight());
        });
    }

    @Override
    public void onExit(World world) {
        if (resizeSubscriptionCancel != null) {
            resizeSubscriptionCancel.run();
            resizeSubscriptionCancel = null;
        }
    }


    @Override
    public void update(World world, int tickCount) {

    }
}
