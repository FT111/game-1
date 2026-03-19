package engine_interfaces.objects.rendering;

public abstract class RenderPass {
        public abstract void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer);
}
