package engine.rendering;

import engine.CoreWorld;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.components.MaskToTileMapComponent;
import engine_interfaces.objects.rendering.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        var textLayers = renderObjects.world().ComponentLayersIndex.query(
                TextComponent.class,
                PositionComponent.class
        );

        textLayers.forEach(textLayer -> {
            HashMap<Class<? extends Component>, Component> textLayerComps = renderObjects.world().Layers.get(textLayer);
            TextComponent textComponent = CoreWorld.getComponent(textLayerComps, TextComponent.class);
            PositionComponent positionComponent = CoreWorld.getComponent(textLayerComps, PositionComponent.class);
            VisibilityComponent visibilityComponent = CoreWorld.getComponent(textLayerComps, VisibilityComponent.class);

            if (visibilityComponent != null && !visibilityComponent.isVisible) { return; }

            String text;
            if (textComponent.text != null) {
                text = textComponent.text;
            } else {
                text = renderObjects.resources().getAsset(
                        textComponent.resourceId,
                        textComponent.assetId,
                        String.class);
            }

            var wrappedText = wrapText(text, textComponent.maxWidth);
            for (int i = 0; i < wrappedText.size(); i++) {
                String line = wrappedText.get(i);
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);
                    Point worldPosition = new Point(positionComponent.Origin.x() + j, positionComponent.Origin.y() + i);
                    Point screenPosition = PositioningCalculators.calc.get(positionComponent.positionStrategy).calculatePosition(
                            worldPosition,
                            textLayer,
                            renderObjects.world(),
                            renderObjects.camera()
                    );
                    // If this text layer is masked by a vision layer, ensure the world position is visible there
                    MaskToTileMapComponent visionMask = CoreWorld.getComponent(textLayerComps, MaskToTileMapComponent.class);
                    if (!RenderPassUtils.isPointVisibleInVisionMask(renderObjects, visionMask, worldPosition.x(), worldPosition.y())) {
                        continue;
                    }

                    if (!renderObjects.camera().isScreenPointInView(screenPosition)) {continue;}

                    var cell = new Cell(c, positionComponent.zIndex);

                    // safe write / z-index collation
                    RenderPassUtils.trySetCell(buffer, screenPosition.x(), screenPosition.y(), cell);
                }
            }

        });
    }

    private List<String> wrapText(String text, int maxWidth) {
        ArrayList<String> output = new ArrayList<>();

        if (maxWidth <= 0 || text == null || text.isEmpty()) {
            output.add(text != null ? text : "");
            return output;
        }

        // handle existing explicit newlines by splitting them first
        String[] lines = text.split("\n");

        for (String logicalLine : lines) {
            StringBuilder currentLine = new StringBuilder();
            String[] words = logicalLine.split(" ");

            for (String word : words) {
                // handle words that are individually longer than maxWidth
                while (word.length() > maxWidth) {
                    if (!currentLine.isEmpty()) {
                        output.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                    output.add(word.substring(0, maxWidth));
                    word = word.substring(maxWidth);
                }

                if (currentLine.length() + word.length() + (currentLine.isEmpty() ? 0 : 1) > maxWidth) {
                    output.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }

            // add remainder
            if (!currentLine.isEmpty()) {
                output.add(currentLine.toString());
            }
        }

        return output;
    }
}
