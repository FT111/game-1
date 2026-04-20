package resources.menus;

import engine.EventBus;
import resources.UiBuilders;
import resources.menus.states.GameHud;
import resources.menus.states.MainMenu;

import java.util.function.Consumer;

public class MenuStates {
    public final GameHud gameHud;
    public final MainMenu mainMenu;
    private final StateContext ctx;

    public MenuStates(Consumer<MenuState> switchTo, UiBuilders ui, EventBus bus) {
        ctx = new StateContext(switchTo, ui, bus, this);

        mainMenu = new MainMenu(ctx);
        gameHud = new GameHud(ctx);

    }
}
