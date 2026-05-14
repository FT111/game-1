package engine;

import engine_interfaces.objects.CameraView;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.components.CameraComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;
import engine_interfaces.objects.rendering.renderObjects;
import engine.layout.LayoutManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class Renderer {
    protected List<RenderPass> renderPasses = new java.util.ArrayList<>();
    private RenderBuffer previousBuffer;
    private RenderBuffer renderBuffer;
    public final GraphicsAPI Api;
    public final LayoutManager layoutManager;

    public Renderer(GraphicsAPI api, LayoutManager layoutManager)
    {
        this.Api = api;
        this.layoutManager = layoutManager;
    }

    // Renders the world to the screen given by the graphics API
    protected void render(World world, Resources resources) throws IOException {
        CameraView cameraView;
        try {
            cameraView = layoutManager.getActiveCameraView();
            if (cameraView == null) {
                throw new RuntimeException("No active camera found in world");
            }
        } catch (RuntimeException e) {
            return;
        }

        renderBuffer = new RenderBuffer(cameraView.width, cameraView.height);

        for (RenderPass pass : renderPasses) {
            pass.render(new renderObjects(world, resources, cameraView, layoutManager), renderBuffer, previousBuffer);
        }

        Api.render(renderBuffer);
        previousBuffer = renderBuffer;
    }

}
