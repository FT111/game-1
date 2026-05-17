package engine.rendering;

import engine.Utils;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.rendering.Cell;
import engine_interfaces.objects.rendering.RenderBuffer;
import engine_interfaces.objects.rendering.renderObjects;
import resources.components.MaskToTileMapComponent;
import engine_interfaces.objects.components.TileMapComponent;

import java.util.List;

/**
 * Small set of helpers to avoid duplicated rendering logic between different render passes.
 */
public final class RenderPassUtils {

    private RenderPassUtils() {}

    public static boolean isLayerInView(renderObjects renderObjects, PositionComponent positionComponent, DimensionsComponent dimensionsComponent) {
        if (positionComponent == null || dimensionsComponent == null) return true;
        if (positionComponent.positionStrategy == null) return true;
        if (positionComponent.positionStrategy.equals(Positioning.FIXED)) return true;
        Point worldEndPoint = new Point(positionComponent.Origin.x() + dimensionsComponent.width, positionComponent.Origin.y() + dimensionsComponent.height);
        return renderObjects.camera().isWorldPointInView(positionComponent.Origin, worldEndPoint);
    }

    public static boolean isPointVisibleInVisionMask(renderObjects renderObjects, MaskToTileMapComponent mask, int worldX, int worldY) {
        if (mask == null) return true;

        var visionLayerComps = renderObjects.world().Layers.get(mask.visionLayer);
        if (visionLayerComps == null) return true;

        var visionTileMapComp = (TileMapComponent) visionLayerComps.get(TileMapComponent.class);
        var visionPosComp = (PositionComponent) visionLayerComps.get(PositionComponent.class);
        if (visionTileMapComp == null || visionPosComp == null) return true;

        try {
            Cell[][] visionAsset = renderObjects.resources().getAsset(visionTileMapComp.resourceId, visionTileMapComp.assetId, Cell[][].class);
            int visX = worldX - visionPosComp.Origin.x();
            int visY = worldY - visionPosComp.Origin.y();
            if (visY < 0 || visY >= visionAsset.length || visX < 0 || visX >= visionAsset[0].length) return false;
            return visionAsset[visY][visX] != null;
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isAnyPointVisibleInVisionMask(renderObjects renderObjects, MaskToTileMapComponent mask, List<Point> worldPoints) {
        if (mask == null) return true;
        for (Point p : worldPoints) {
            if (isPointVisibleInVisionMask(renderObjects, mask, p.x(), p.y())) return true;
        }
        return false;
    }

    public static void trySetCell(RenderBuffer buffer, int screenX, int screenY, Cell newCell) {
        try {
            Cell existingCell = buffer.cells[screenY][screenX];
            Cell out = Utils.collateCells(existingCell, newCell);
            buffer.cells[screenY][screenX] = out;
        } catch (ArrayIndexOutOfBoundsException e) {
            // out of bounds - ignore
        }
    }
}

