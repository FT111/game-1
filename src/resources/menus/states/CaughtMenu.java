package resources.menus.states;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.events.PushSceneEvent;
import engine_interfaces.objects.rendering.Colour;
import resources.StyledUI;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class CaughtMenu extends MenuState {
    public CaughtMenu(StateContext ctx) {
        super(ctx);

        String caughtText = "You were caught!";
        var label = ctx.ui().new LabelBuilder<>()
                .withStaticText(caughtText)
                .withPosition(new Point(0, -1), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(caughtText.length(), 1)
                .build();

        String toMenu = "Return to Main Menu";
        var quitButton = StyledUI.menuButton(ctx.ui())
                .withStaticText(toMenu)
                .withPosition(new Point(0, 2), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(toMenu.length(), 1)
                .onClick((btn) -> {
                    // go back to main menu (pop gameplay and push main menu)
                    ctx.bus().publish(new PopSceneEvent());
                    ctx.bus().publish(new PushSceneEvent("MainMenu"));
                    ctx.switchTo().accept(ctx.states().mainMenu);
                })
                .build();

        show(label);
        show(quitButton);
    }
}

