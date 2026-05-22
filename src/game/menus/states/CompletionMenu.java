package game.menus.states;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.events.PushSceneEvent;
import game.StyledUI;
import game.menus.MenuState;
import game.menus.StateContext;

public class CompletionMenu extends MenuState {
    public CompletionMenu(StateContext ctx) {
        super(ctx);

        String panelText = """
                ╔════════════════════════════════════╗
                ║                                    ║
                ║            LEVEL COMPLETE          ║
                ║                                    ║
                ║     You have successfully escaped  ║
                ║           with the data            ║
                ║                                    ║
                ╚════════════════════════════════════╝""";

        var panel = ctx.ui().new LabelBuilder()
                .withStaticText(panelText)
                .withPosition(new Point(0, -3), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(40, 9)
                .build();

        String toMenu = "Return to Main Menu";
        var menuButton = StyledUI.menuButton(ctx.ui())
                .withStaticText(toMenu)
                .withPosition(new Point(0, 4), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(toMenu.length(), 1)
                .onClick((btn) -> {
                    ctx.bus().publish(new PopSceneEvent());
                    ctx.bus().publish(new PushSceneEvent("MainMenu"));
                    ctx.switchTo().accept(ctx.states().mainMenu);
                })
                .build();

        show(panel);
        show(menuButton);
    }
}
