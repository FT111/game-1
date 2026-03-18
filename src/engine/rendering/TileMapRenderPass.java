package engine.rendering;

import engine.World;
import engine_interfaces.objects.CameraView;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;

import java.util.HashSet;

public class TileMapRenderPass extends RenderPass {

    @Override
    public void render(World world, CameraView camera, RenderBuffer buffer, RenderBuffer previousBuffer) {
        HashSet<EntityID> tileMapEntities = world.queryEntities(TileMapComponent.class);
    }
}
