package engine.rendering;

import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;
import engine_interfaces.objects.rendering.renderObjects;

import java.util.HashSet;

public class TileMapRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        HashSet<EntityID> tileMapEntities = renderObjects.world().queryEntities(TileMapComponent.class);
    }
}
