package engine;

import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;

public class Renderer {
    private RenderBuffer renderBuffer;
    private GraphicsAPI Api;

    public Renderer(GraphicsAPI api)
    {
        this.Api = api;
    }


}
