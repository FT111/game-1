package resources.menus.states;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.rendering.Colour;
import engine_interfaces.objects.components.BackgroundComponent;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class MainMenu extends MenuState {
    public MainMenu(StateContext stateContext) {
        String continueText = "Continue";

        var label = stateContext.ui().new LabelBuilder()
                .withStaticText("Menu")
                .withPosition(new Point(0, -2), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions("Menu".length(), 1)
                .build();

        var continueButton = stateContext.ui().new ButtonBuilder()
                .withStaticText(continueText)
                .withPosition(new Point(0,2), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withBackground(new Colour(120, 120, 120), null, -1)
                .withDimensions(continueText.length(), 1)
                .build();

        show(continueButton);
        show(label);
        bindClick(continueButton, () -> {stateContext.switchTo().accept(stateContext.states().gameHud);
            stateContext.bus().publish(new engine_interfaces.objects.events.SwitchSceneEvent("Gameplay"));
        });

        bindHoverEnter(continueButton, () -> {
            var bg = (BackgroundComponent) stateContext.elementComponents().apply(continueButton).get(BackgroundComponent.class);
            if (bg != null) {
                bg.bgColour = new Colour(15, 15, 15);
            }
        });

        bindHoverExit(continueButton, () -> {
            var bg = (BackgroundComponent) stateContext.elementComponents().apply(continueButton).get(BackgroundComponent.class);
            if (bg != null) {
                bg.bgColour = new Colour(120, 120, 120);
            }
        });
    }

}
