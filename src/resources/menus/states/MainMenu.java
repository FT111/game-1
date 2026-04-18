package resources.menus.states;

import engine_interfaces.objects.Point;
import resources.UiBuilders;
import resources.menus.MenuState;

import java.util.function.Consumer;

public class MainMenu extends MenuState {
    public MainMenu(Consumer<MenuState> switchTo, UiBuilders ui) {
        var continueButton = ui.new ButtonBuilder()
                .withStaticText("Continue")
                .withPosition(new Point(10,10))
                .build();

        show(continueButton);
    }


}
