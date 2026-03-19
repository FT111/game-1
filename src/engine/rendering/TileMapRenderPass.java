package engine.rendering;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.EntityID;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TileMapComponent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.RenderPass;
import engine_interfaces.objects.rendering.renderObjects;

import java.util.HashSet;

public class TileMapRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        HashSet tileMapEntities = renderObjects.world().ComponentLayersIndex.query(new Class[] {TileMapComponent.class, PositionComponent.class});

        tileMapEntities.forEach(layerID -> {
            // Get the tile map and position components for this layer
            TileMapComponent tileMapComponent = (TileMapComponent) renderObjects.world().Layers.get(layerID).get(TileMapComponent.class);
            PositionComponent positionComponent = (PositionComponent) renderObjects.world().Layers.get(layerID).get(PositionComponent.class);

            if (!tileMapComponent.isVisible) {
                return;
            }

            Cell[][] tileMap = renderObjects.resources().getAsset(
                    tileMapComponent.resourceId,
                    tileMapComponent.assetId,
                    Cell[][].class);
            Point tileEndPoint = new Point(positionComponent.Origin.x() + tileMap[0].length, positionComponent.Origin.y() + tileMap.length);


            // Cull tile map if it is not in view of the camera
            if (!renderObjects.camera().isInView(positionComponent.Origin, tileEndPoint)) {
                return;
            }

            // Render the tile map to the buffer, culling individual cells that are not in view of the camera
            for (int y = 0; y < tileMap.length; y++) {
                for (int x = 0; x < tileMap[0].length; x++) {
                    Cell cell = tileMap[y][x];
                    if (cell == null) {
                        continue;
                    }
                    Point cellPosition = new Point(positionComponent.Origin.x() + x, positionComponent.Origin.y() + y);
                    if (!renderObjects.camera().isInView(cellPosition, cellPosition)) {
                        continue;
                    }

                    // Skip empty cells -- Allows transparency
                    if (cell.content == ' ') continue;

                    // Make cells relative to the camera position
                    Point relativePoint = renderObjects.camera().worldToScreen(new Point(x, y));
                    buffer.cells[relativePoint.y()][relativePoint.x()] = cell;
                }
            }
        });
    }
}
