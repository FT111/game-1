package engine.rendering;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.components.PositionComponent;
import engine_interfaces.objects.components.TextComponent;
import engine_interfaces.objects.rendering.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TextRenderPass extends RenderPass {

    @Override
    public void render(renderObjects renderObjects, RenderBuffer buffer, RenderBuffer previousBuffer) {
        var textLayers = (HashSet<LayerID>) renderObjects.world().ComponentLayersIndex.query(new Class[] {TextComponent.class, PositionComponent.class});

        textLayers.forEach(textLayer -> {
            HashMap<Class<? extends Component>, Component> textLayerComps = renderObjects.world().Layers.get(textLayer);
            TextComponent textComponent = (TextComponent) textLayerComps.get(TextComponent.class);
            PositionComponent positionComponent = (PositionComponent) textLayerComps.get(PositionComponent.class);

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
                    if (!renderObjects.camera().isScreenPointInView(screenPosition)) {continue;}

                    // handle z index
                    try {
                        var existingCell = buffer.cells[screenPosition.y()][screenPosition.x()];
                        if (existingCell != null && existingCell.zIndex > positionComponent.zIndex) {
                            continue;
                    }}  catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    }

                    buffer.cells[screenPosition.y()][screenPosition.x()] = new Cell(c, positionComponent.zIndex);
                }
            }

        });
    }

    private List<String> wrapText(String text, int maxWidth) {
        ArrayList<String> output = new ArrayList<>();

        // base case where text wrapping is disabled
        if (maxWidth == 0) {
            output.add(text);
            return output;
        }

        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split(" ")) {
            if (currentLine.length() + word.length() + 1 > maxWidth) {
                output.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(" ").append(word);
            }
        }
        return output;
    }
}
