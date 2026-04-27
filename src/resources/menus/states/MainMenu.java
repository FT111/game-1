package resources.menus.states;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class MainMenu extends MenuState {
    public MainMenu(StateContext stateContext) {
        String continueText = "Continue";
        var continueButton = stateContext.ui().new ButtonBuilder()
                .withStaticText(continueText)
                .withPosition(new Point(50,15), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(continueText.length()+2, 1)
                .build();

        show(continueButton);
        bind(continueButton, () -> {stateContext.switchTo().accept(stateContext.states().gameHud);
            stateContext.bus().publish(new engine_interfaces.objects.events.SwitchSceneEvent("Gameplay"));
        });
    }


}
