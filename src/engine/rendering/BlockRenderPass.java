package engine.rendering;

import engine.Utils;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.DimensionsComponent;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.components.BackgroundComponent;
import engine_interfaces.objects.components.BorderComponent;
import engine_interfaces.objects.rendering.*;

import java.util.HashMap;
import java.util.HashSet;

public class BlockRenderPass extends RenderPass {

	@Override
	public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
		// Find all layers that have a position and dimensions (candidates for backgrounds/borders)
		HashSet<LayerID> layers = (HashSet<LayerID>) renderObjects.world().ComponentLayersIndex.query(new Class[]{PositionComponent.class, DimensionsComponent.class});

		layers.forEach(layerID -> {
			HashMap<Class<? extends Component>, Component> components = renderObjects.world().Layers.get(layerID);
			PositionComponent positionComponent = (PositionComponent) components.get(PositionComponent.class);
			DimensionsComponent dimensionsComponent = (DimensionsComponent) components.get(DimensionsComponent.class);
			VisibilityComponent visibilityComponent = (VisibilityComponent) components.get(VisibilityComponent.class);

			BackgroundComponent backgroundComponent = (BackgroundComponent) components.get(BackgroundComponent.class);
			BorderComponent borderComponent = (BorderComponent) components.get(BorderComponent.class);

			// If neither background nor border present, nothing for this pass to do
			if (backgroundComponent == null && borderComponent == null) { return; }

			if (visibilityComponent != null && !visibilityComponent.isVisible) { return; }

			// Cull using the world's coordinates for non-fixed layers
			Point worldEndPoint = new Point(positionComponent.Origin.x() + dimensionsComponent.width, positionComponent.Origin.y() + dimensionsComponent.height);
			if (!positionComponent.positionStrategy.equals(Positioning.FIXED) && !renderObjects.camera().isWorldPointInView(positionComponent.Origin, worldEndPoint)) {
				return;
			}

			var screenOrigin = renderObjects.layoutManager().getCalculatedScreenPosition(layerID, renderObjects.camera());
			if (screenOrigin == null) return;

			var screenX = screenOrigin.x();
			var screenY = screenOrigin.y();

			// Render background fill if present
			if (backgroundComponent != null && backgroundComponent.enabled) {
				for (int y = 0; y < dimensionsComponent.height; y++) {
					for (int x = 0; x < dimensionsComponent.width; x++) {
						var screenPoint = new Point(screenOrigin.x() + x, screenOrigin.y() + y);
						if (!renderObjects.camera().isScreenPointInView(screenPoint)) { continue; }

						try {
							Cell existingCell = buffer.cells[screenPoint.y()][screenPoint.x()];
							Cell fillCell = new Cell(
									backgroundComponent.fillChar == null ? ' ' : backgroundComponent.fillChar,
									null,
									backgroundComponent.bgColour,
									backgroundComponent.zIndex
							);
							fillCell = Utils.collateCells(existingCell, fillCell);
							buffer.cells[screenPoint.y()][screenPoint.x()] = fillCell;
						} catch (ArrayIndexOutOfBoundsException e) {
							continue;
						}
					}
				}
			}

			// Render border if present
			if (borderComponent != null && borderComponent.enabled) {
				int thickness = Math.max(1, borderComponent.thickness);

				for (int t = 0; t < thickness; t++) {
					int topY = t;
					int bottomY = dimensionsComponent.height - 1 - t;
					int leftX = t;
					int rightX = dimensionsComponent.width - 1 - t;

					// horizontal edges
					for (int x = leftX; x <= rightX; x++) {
						drawBorderCell(buffer, renderObjects, screenOrigin.x() + x, screenOrigin.y() + topY, borderComponent.horizontalChar, borderComponent.fgColour, borderComponent.zIndex);
						drawBorderCell(buffer, renderObjects, screenOrigin.x() + x, screenOrigin.y() + bottomY, borderComponent.horizontalChar, borderComponent.fgColour, borderComponent.zIndex);
					}

					// vertical edges (skip corners which are already drawn)
					for (int y = topY + 1; y <= bottomY - 1; y++) {
						drawBorderCell(buffer, renderObjects, screenOrigin.x() + leftX, screenOrigin.y() + y, borderComponent.verticalChar, borderComponent.fgColour, borderComponent.zIndex);
						drawBorderCell(buffer, renderObjects, screenOrigin.x() + rightX, screenOrigin.y() + y, borderComponent.verticalChar, borderComponent.fgColour, borderComponent.zIndex);
					}

					// corners (use corner char if provided, else fallback to horizontalChar)
					Character cornerChar = borderComponent.cornerChar != null ? borderComponent.cornerChar : borderComponent.horizontalChar;
					drawBorderCell(buffer, renderObjects, screenOrigin.x() + leftX, screenOrigin.y() + topY, cornerChar, borderComponent.fgColour, borderComponent.zIndex);
					drawBorderCell(buffer, renderObjects, screenOrigin.x() + rightX, screenOrigin.y() + topY, cornerChar, borderComponent.fgColour, borderComponent.zIndex);
					drawBorderCell(buffer, renderObjects, screenOrigin.x() + leftX, screenOrigin.y() + bottomY, cornerChar, borderComponent.fgColour, borderComponent.zIndex);
					drawBorderCell(buffer, renderObjects, screenOrigin.x() + rightX, screenOrigin.y() + bottomY, cornerChar, borderComponent.fgColour, borderComponent.zIndex);
				}
			}
		});
	}

	private void drawBorderCell(RenderBuffer buffer, renderObjects renderObjects, int screenX, int screenY, Character ch, engine_interfaces.objects.rendering.Colour fg, int zIndex) {
		Point screenPoint = new Point(screenX, screenY);
		if (!renderObjects.camera().isScreenPointInView(screenPoint)) { return; }

		try {
			Cell existingCell = buffer.cells[screenPoint.y()][screenPoint.x()];

			// Respect z-index ordering
			if (existingCell != null && existingCell.zIndex > zIndex) {
				return;
			}

			Cell newCell = new Cell(ch == null ? ' ' : ch, fg, null, zIndex);
			buffer.cells[screenPoint.y()][screenPoint.x()] = Utils.collateCells(existingCell, newCell);
		} catch (ArrayIndexOutOfBoundsException e) {
			// out of bounds of buffer - ignore
		}
	}
}

