package resources.menus.states;

import engine_interfaces.objects.LayerID;
import resources.menus.KeyInputBind;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class GameHud extends MenuState {
    public GameHud(StateContext ctx) {
        bind(new KeyInputBind('p'), () -> ctx.switchTo().accept(ctx.states().mainMenu));
    }
}
