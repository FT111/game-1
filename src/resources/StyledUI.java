package resources;

import engine_interfaces.objects.components.BackgroundComponent;
import engine_interfaces.objects.rendering.Colour;

public class StyledUI {
    public static UiBuilders.ButtonBuilder menuButton(UiBuilders ui) {
        return ui.new ButtonBuilder()
                .withBackground(new Colour(120, 120, 120), null, -1)
                .onHoverEnter((button) -> {
                    var bg = (BackgroundComponent) button.get(BackgroundComponent.class);
                    if (bg != null) {
                        bg.bgColour = new Colour(15, 15, 15);
                    }})
                .onHoverExit((button) -> {
                    var bg = (BackgroundComponent) button.get(BackgroundComponent.class);
                    if (bg != null) {
                        bg.bgColour = new Colour(120, 120, 120);
                    }});
    }
}
