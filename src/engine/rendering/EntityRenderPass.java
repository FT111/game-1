package engine.rendering;

import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.RenderableComponent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;
import engine_interfaces.objects.rendering.renderObjects;

public class EntityRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        var entities = renderObjects.world().ComponentEntitiesIndex.query(RenderableComponent.class);

        // Switch this to a chunk-based culling approach in the future
        entities.forEach(entity -> {
            var renderDetails = (RenderableComponent) renderObjects.world().Entities.get(entity).get(RenderableComponent.class);
            var positionDetails = (PositionComponent) renderObjects.world().Entities.get(entity).get(PositionComponent.class);

            if (!renderDetails.isVisible) {
                return;
            }

            if (!renderObjects.camera().isInView(positionDetails.Origin)) {
                return;
            }

            // Make the entity's position relative to the camera position
            var relativePoint = renderObjects.camera().worldToScreen(positionDetails.Origin);

            buffer.cells[relativePoint.y()][relativePoint.x()] = new Cell(renderDetails.Char, renderDetails.fgColour, renderDetails.bgColour);
        });
    }


}
