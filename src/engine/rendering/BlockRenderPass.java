package engine.rendering;

import engine.CoreWorld;
import engine.Utils;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.*;
import engine_interfaces.objects.components.MaskToTileMapComponent;
import engine_interfaces.objects.rendering.*;

import java.util.HashMap;
import java.util.HashSet;

public class BlockRenderPass extends RenderPass {

	@Override
	public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
		// Find all layers that have a position and dimensions (candidates for backgrounds/borders)
		HashSet<LayerID> layers = renderObjects.world().ComponentLayersIndex.query(
				PositionComponent.class,
				DimensionsComponent.class
		);

		layers.forEach(layerID -> {
			HashMap<Class<? extends Component>, Component> components = renderObjects.world().Layers.get(layerID);
			PositionComponent positionComponent = CoreWorld.getComponent(components, PositionComponent.class);
			DimensionsComponent dimensionsComponent = CoreWorld.getComponent(components, DimensionsComponent.class);
			VisibilityComponent visibilityComponent = CoreWorld.getComponent(components, VisibilityComponent.class);

			BackgroundComponent backgroundComponent = CoreWorld.getComponent(components, BackgroundComponent.class);
			BorderComponent borderComponent = CoreWorld.getComponent(components, BorderComponent.class);

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

			// Determine if this layer is vision-masked
			MaskToTileMapComponent visionMask = CoreWorld.getComponent(components, MaskToTileMapComponent.class);

			// Render background fill if present
			if (backgroundComponent != null && backgroundComponent.enabled) {
				for (int y = 0; y < dimensionsComponent.height; y++) {
					for (int x = 0; x < dimensionsComponent.width; x++) {
						var screenPoint = new Point(screenOrigin.x() + x, screenOrigin.y() + y);
						// world coords for this cell
						int worldX = positionComponent.Origin.x() + x;
						int worldY = positionComponent.Origin.y() + y;

						// If vision-masked, ensure visible in vision layer
						if (visionMask != null) {
							var visionLayerComps = renderObjects.world().Layers.get(visionMask.visionLayer);
							if (visionLayerComps != null) {
								var visionTileMapComp = CoreWorld.getComponent(visionLayerComps, TileMapComponent.class);
								var visionPosComp = CoreWorld.getComponent(visionLayerComps, PositionComponent.class);
								var visionDimsComp = CoreWorld.getComponent(visionLayerComps, DimensionsComponent.class);
								if (visionTileMapComp != null && visionPosComp != null && visionDimsComp != null) {
									try {
										Cell[][] visionAsset = renderObjects.resources().getAsset(visionTileMapComp.resourceId, visionTileMapComp.assetId, Cell[][].class);
										int visX = worldX - visionPosComp.Origin.x();
										int visY = worldY - visionPosComp.Origin.y();
										if (visY < 0 || visY >= visionAsset.length || visX < 0 || visX >= visionAsset[0].length || visionAsset[visY][visX] == null) {
											continue;
										}
									} catch (Exception e) {
										// fall through and render if vision check fails
									}
								}
							}
						}

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
						// compute world coords for border positions
						int wxTop = positionComponent.Origin.x() + x;
						int wyTop = positionComponent.Origin.y() + topY;
						int wxBottom = positionComponent.Origin.x() + x;
						int wyBottom = positionComponent.Origin.y() + bottomY;

						// if vision-masked, skip cells not visible
						if (visionMask != null) {
							var visionLayerComps = renderObjects.world().Layers.get(visionMask.visionLayer);
							if (visionLayerComps != null) {
								var visionTileMapComp = CoreWorld.getComponent(visionLayerComps, TileMapComponent.class);
								var visionPosComp = CoreWorld.getComponent(visionLayerComps, PositionComponent.class);
								if (visionTileMapComp != null && visionPosComp != null) {
									try {
										Cell[][] visionAsset = renderObjects.resources().getAsset(visionTileMapComp.resourceId, visionTileMapComp.assetId, Cell[][].class);
										int visTopX = wxTop - visionPosComp.Origin.x();
										int visTopY = wyTop - visionPosComp.Origin.y();
										int visBotX = wxBottom - visionPosComp.Origin.x();
										int visBotY = wyBottom - visionPosComp.Origin.y();
										boolean topVisible = !(visTopY < 0 || visTopY >= visionAsset.length || visTopX < 0 || visTopX >= visionAsset[0].length || visionAsset[visTopY][visTopX] == null);
										boolean botVisible = !(visBotY < 0 || visBotY >= visionAsset.length || visBotX < 0 || visBotX >= visionAsset[0].length || visionAsset[visBotY][visBotX] == null);
										if (!topVisible && !botVisible) {
											continue;
										}
									} catch (Exception e) {
										// fall through and render
									}
								}
							}
						}

						drawBorderCell(buffer, renderObjects, screenOrigin.x() + x, screenOrigin.y() + topY, borderComponent.horizontalChar, borderComponent.fgColour, borderComponent.zIndex);
						drawBorderCell(buffer, renderObjects, screenOrigin.x() + x, screenOrigin.y() + bottomY, borderComponent.horizontalChar, borderComponent.fgColour, borderComponent.zIndex);
					}

					// vertical edges (skip corners which are already drawn)
					for (int y = topY + 1; y <= bottomY - 1; y++) {
						int wxLeft = positionComponent.Origin.x() + leftX;
						int wyLeft = positionComponent.Origin.y() + y;
						int wxRight = positionComponent.Origin.x() + rightX;
						int wyRight = positionComponent.Origin.y() + y;

						if (visionMask != null) {
							var visionLayerComps = renderObjects.world().Layers.get(visionMask.visionLayer);
							if (visionLayerComps != null) {
								var visionTileMapComp = CoreWorld.getComponent(visionLayerComps, TileMapComponent.class);
								var visionPosComp = CoreWorld.getComponent(visionLayerComps, PositionComponent.class);
								if (visionTileMapComp != null && visionPosComp != null) {
									try {
										Cell[][] visionAsset = renderObjects.resources().getAsset(visionTileMapComp.resourceId, visionTileMapComp.assetId, Cell[][].class);
										int visLeftX = wxLeft - visionPosComp.Origin.x();
										int visLeftY = wyLeft - visionPosComp.Origin.y();
										int visRightX = wxRight - visionPosComp.Origin.x();
										int visRightY = wyRight - visionPosComp.Origin.y();
										boolean leftVisible = !(visLeftY < 0 || visLeftY >= visionAsset.length || visLeftX < 0 || visLeftX >= visionAsset[0].length || visionAsset[visLeftY][visLeftX] == null);
										boolean rightVisible = !(visRightY < 0 || visRightY >= visionAsset.length || visRightX < 0 || visRightX >= visionAsset[0].length || visionAsset[visRightY][visRightX] == null);
										if (!leftVisible && !rightVisible) {
											continue;
										}
									} catch (Exception e) {
									}
								}
							}
						}

						drawBorderCell(buffer, renderObjects, screenOrigin.x() + leftX, screenOrigin.y() + y, borderComponent.verticalChar, borderComponent.fgColour, borderComponent.zIndex);
						drawBorderCell(buffer, renderObjects, screenOrigin.x() + rightX, screenOrigin.y() + y, borderComponent.verticalChar, borderComponent.fgColour, borderComponent.zIndex);
					}

					// corners (use corner char if provided, else fallback to horizontalChar)
					Character cornerChar = borderComponent.cornerChar != null ? borderComponent.cornerChar : borderComponent.horizontalChar;
					// corners
					int wx1 = positionComponent.Origin.x() + leftX;
					int wy1 = positionComponent.Origin.y() + topY;
					int wx2 = positionComponent.Origin.x() + rightX;
					int wy2 = positionComponent.Origin.y() + topY;
					int wx3 = positionComponent.Origin.x() + leftX;
					int wy3 = positionComponent.Origin.y() + bottomY;
					int wx4 = positionComponent.Origin.x() + rightX;
					int wy4 = positionComponent.Origin.y() + bottomY;

					if (visionMask != null) {
						var visionLayerComps = renderObjects.world().Layers.get(visionMask.visionLayer);
						if (visionLayerComps != null) {
							var visionTileMapComp = CoreWorld.getComponent(visionLayerComps, TileMapComponent.class);
							var visionPosComp = CoreWorld.getComponent(visionLayerComps, PositionComponent.class);
							if (visionTileMapComp != null && visionPosComp != null) {
								try {
									Cell[][] visionAsset = renderObjects.resources().getAsset(visionTileMapComp.resourceId, visionTileMapComp.assetId, Cell[][].class);
									int v1x = wx1 - visionPosComp.Origin.x(); int v1y = wy1 - visionPosComp.Origin.y();
									int v2x = wx2 - visionPosComp.Origin.x(); int v2y = wy2 - visionPosComp.Origin.y();
									int v3x = wx3 - visionPosComp.Origin.x(); int v3y = wy3 - visionPosComp.Origin.y();
									int v4x = wx4 - visionPosComp.Origin.x(); int v4y = wy4 - visionPosComp.Origin.y();
									boolean c1 = !(v1y < 0 || v1y >= visionAsset.length || v1x < 0 || v1x >= visionAsset[0].length || visionAsset[v1y][v1x] == null);
									boolean c2 = !(v2y < 0 || v2y >= visionAsset.length || v2x < 0 || v2x >= visionAsset[0].length || visionAsset[v2y][v2x] == null);
									boolean c3 = !(v3y < 0 || v3y >= visionAsset.length || v3x < 0 || v3x >= visionAsset[0].length || visionAsset[v3y][v3x] == null);
									boolean c4 = !(v4y < 0 || v4y >= visionAsset.length || v4x < 0 || v4x >= visionAsset[0].length || visionAsset[v4y][v4x] == null);
									if (!c1 && !c2 && !c3 && !c4) {
										// none of the corners visible
										continue;
									}
								} catch (Exception e) {
								}
							}
						}
					}

					drawBorderCell(buffer, renderObjects, screenOrigin.x() + leftX, screenOrigin.y() + topY, cornerChar, borderComponent.fgColour, borderComponent.zIndex);
					drawBorderCell(buffer, renderObjects, screenOrigin.x() + rightX, screenOrigin.y() + topY, cornerChar, borderComponent.fgColour, borderComponent.zIndex);
					drawBorderCell(buffer, renderObjects, screenOrigin.x() + leftX, screenOrigin.y() + bottomY, cornerChar, borderComponent.fgColour, cornerChar == null ? borderComponent.zIndex : borderComponent.zIndex);
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
