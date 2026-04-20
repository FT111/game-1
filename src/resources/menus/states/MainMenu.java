package resources.menus.states;

import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class MainMenu extends MenuState {
    public MainMenu(StateContext stateContext) {
        String continueText = "Continue";
        var continueButton = stateContext.ui().new ButtonBuilder()
                .withStaticText(continueText)
                .withPosition(new Point(20,15), Positioning.FIXED)
                .withDimensions(continueText.length()+2, 1)
                .build();

        show(continueButton);
        bind(continueButton, () -> {stateContext.switchTo().accept(stateContext.states().gameHud);});
    }


}
