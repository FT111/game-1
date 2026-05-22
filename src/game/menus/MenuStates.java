package game.menus;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import game.UiBuilders;
import game.menus.states.GameHud;
import game.menus.states.MainMenu;
import game.menus.states.CompletionMenu;
import game.menus.states.MenuGate;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuStates {
    public final GameHud gameHud;
    public final MainMenu mainMenu;
    public final game.menus.states.CaughtMenu caughtMenu;
    public final CompletionMenu completionMenu;
    public final MenuGate menuGate;

    public MenuStates(Consumer<MenuState> switchTo, UiBuilders ui, EventBus bus, World world, Function<LayerID, HashMap<Class<? extends Component>, Component>> elementComponents) {

        mainMenu = new MainMenu(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

        gameHud = new GameHud(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

        caughtMenu = new game.menus.states.CaughtMenu(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

        completionMenu = new CompletionMenu(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

        menuGate = new MenuGate(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

    }
}
