package resources.menus;

import engine.EventBus;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import resources.UiBuilders;
import resources.menus.states.GameHud;
import resources.menus.states.MainMenu;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuStates {
    public final GameHud gameHud;
    public final MainMenu mainMenu;
    private final StateContext ctx;

    public MenuStates(Consumer<MenuState> switchTo, UiBuilders ui, EventBus bus, Function<LayerID, HashMap<Class<? extends Component>, Component>> elementComponents) {
        ctx = new StateContext(switchTo, ui, bus, this, elementComponents);

        mainMenu = new MainMenu(ctx);
        gameHud = new GameHud(ctx);

    }
}
