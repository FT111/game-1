package engine;

import engine_interfaces.objects.CameraView;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;
import engine_interfaces.objects.rendering.renderObjects;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class Renderer {
    protected List<RenderPass> renderPasses = new java.util.ArrayList<>();
    private RenderBuffer previousBuffer;
    private RenderBuffer renderBuffer;
    public final GraphicsAPI Api;

    public Renderer(GraphicsAPI api)
    {
        this.Api = api;
    }

    // Renders the world to the screen given by the graphics API
    protected void render(World world, Resources resources) throws IOException {
        CameraView cameraView;
        try {
            cameraView = getCameraView(world);
        } catch (RuntimeException e) {
            return;
        }

        renderBuffer = new RenderBuffer(cameraView.width, cameraView.height);


        for (RenderPass pass : renderPasses) {
            pass.render(new renderObjects(world, resources, cameraView), renderBuffer, previousBuffer);
        }

        Api.render(renderBuffer);
        previousBuffer = renderBuffer;
    }

    private static CameraView getCameraView(World world) {
        HashSet<EntityID> cameraEntities = world.ComponentEntitiesIndex.query(CameraComponent.class);
        CameraComponent activeCameraDetails = null;
        PositionComponent activeCameraPosition = null;

        for (EntityID entity : cameraEntities) {
            CameraComponent camera = (CameraComponent) world.Entities.get(entity).get(CameraComponent.class);
            if (camera.isActive) {
                if (activeCameraDetails != null) {
                    throw new RuntimeException("Multiple active cameras found in world");
                }
                activeCameraDetails = camera;
                activeCameraPosition = (PositionComponent) world.Entities.get(entity).get(PositionComponent.class);
            }
        }

        if (activeCameraDetails == null) {
            throw new RuntimeException("No active camera found in world");
        }
        return new CameraView(activeCameraPosition.Origin.x(), activeCameraPosition.Origin.y(), activeCameraDetails.viewWidth,activeCameraDetails.viewHeight);
    }

}
