package resources.menus;

import resources.UiBuilders;
import resources.menus.states.MainMenu;

import java.util.function.Consumer;

public class MenuStates {
    public MainMenu mainMenu;

    public MenuStates(Consumer<MenuState> switchTo, UiBuilders ui) {
        mainMenu = new MainMenu(switchTo, ui);
    }
}
