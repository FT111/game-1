package resources.menus.states;

import engine_interfaces.objects.events.SwitchSceneEvent;
import resources.menus.KeyInputBind;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class GameHud extends MenuState {
    public GameHud(StateContext ctx) {
        super(ctx);
        bindKeypress(new KeyInputBind('p'), () -> {
            ctx.switchTo().accept(ctx.states().mainMenu);
                ctx.bus().publish(new SwitchSceneEvent("MainMenu"));
        });
    }
}
