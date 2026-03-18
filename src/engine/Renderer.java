package engine;

import engine_interfaces.objects.CameraView;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;

import java.io.IOException;
import java.util.List;

public class Renderer {
    protected List<RenderPass> renderPasses;
    private RenderBuffer previousBuffer;
    private RenderBuffer renderBuffer;
    private final GraphicsAPI Api;

    public Renderer(GraphicsAPI api)
    {
        this.Api = api;
    }

    // Renders the world to the screen given by the graphics API
    protected void render(World world) throws IOException {
        CameraView cameraView = getCameraView(world);

        for (RenderPass pass : renderPasses) {
            pass.render(world, cameraView, renderBuffer, previousBuffer);
        }

        Api.render(renderBuffer);
        previousBuffer = renderBuffer;
    }

    private static CameraView getCameraView(World world) {
        List<EntityID> cameraEntities = world.ComponentEntitiesIndex.get(CameraComponent.class);
        CameraComponent activeCamera = null;

        for (EntityID entity : cameraEntities) {
            CameraComponent camera = (CameraComponent) world.Entities.get(entity).get(CameraComponent.class);
            if (camera.isActive) {
                if (activeCamera != null) {
                    throw new RuntimeException("Multiple active cameras found in world");
                }
                activeCamera = camera;
            }
        }

        if (activeCamera == null) {
            throw new RuntimeException("No active camera found in world");
        }
        return new CameraView(activeCamera.Origin.x(), activeCamera.Origin.y(), activeCamera.viewWidth,activeCamera.viewHeight);
    }

}
