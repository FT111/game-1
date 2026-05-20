package engine.rendering;

import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.RenderableComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import resources.components.MaskToTileMapComponent;
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
            var renderDetails = renderObjects.world().getEntityComponent(entity, RenderableComponent.class);
            var positionDetails = renderObjects.world().getEntityComponent(entity, PositionComponent.class);
            var visibilityDetails = renderObjects.world().getEntityComponent(entity, VisibilityComponent.class);

            if (visibilityDetails != null && !visibilityDetails.isVisible) {
                return;
            }

            if (!renderObjects.camera().isWorldPointInView(positionDetails.Origin)) {
                return;
            }

            // If this entity has a vision-mask component, only render if the entity's world position
            // is visible in the referenced vision layer
            MaskToTileMapComponent visionMask = renderObjects.world().getEntityComponent(entity, MaskToTileMapComponent.class);
            if (!RenderPassUtils.isPointVisibleInVisionMask(renderObjects, visionMask, positionDetails.Origin.x(), positionDetails.Origin.y())) {
                return;
            }

            // Make the entity's position relative to the camera position
            var relativePoint = renderObjects.camera().worldToScreen(positionDetails.Origin);

            buffer.cells[relativePoint.y()][relativePoint.x()] = new Cell(renderDetails.Char, renderDetails.fgColour, renderDetails.bgColour);
        });
    }


}
