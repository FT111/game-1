package resources;

import engine.World;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.rendering.GraphicsAPI;

import java.io.Console;

public class TestSystem extends System {
    EntityID level;

    public TestSystem(EntityID level, GraphicsAPI api, World world) {
        super();
        this.level = level;

        api.onResize( () -> {
            CameraComponent camera = (CameraComponent) world.Entities.get(level).get(CameraComponent.class);
            camera.viewWidth = api.getWidth();
            camera.viewHeight = api.getHeight();
            IO.println("Resized to: " + api.getWidth() + "x" + api.getHeight());
        });
    }


    @Override
    public void update(World world, int tickCount) {

    }
}
