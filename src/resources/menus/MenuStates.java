package resources.menus;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.LayerID;
import resources.UiBuilders;
import resources.menus.states.GameHud;
import resources.menus.states.MainMenu;
import resources.menus.states.MenuGate;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuStates {
    public final GameHud gameHud;
    public final MainMenu mainMenu;
    public final MenuGate menuGate;

    public MenuStates(Consumer<MenuState> switchTo, UiBuilders ui, EventBus bus, World world, Function<LayerID, HashMap<Class<? extends Component>, Component>> elementComponents) {

        mainMenu = new MainMenu(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

        gameHud = new GameHud(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

        menuGate = new MenuGate(new StateContext(switchTo, new UiBuilders(world), bus, this, elementComponents));

    }
}
