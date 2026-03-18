package engine_interfaces.objects.rendering;

import engine.World;
import engine_interfaces.objects.CameraView;

public abstract class RenderPass {
        public abstract void render(World world, CameraView camera, RenderBuffer buffer, RenderBuffer previousBuffer);
}
