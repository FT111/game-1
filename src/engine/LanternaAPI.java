package engine;

import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;

public class LanternaAPI implements GraphicsAPI {
    @Override
    public void render(RenderBuffer buffer) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void onResize(Runnable callback) {

    }
}
