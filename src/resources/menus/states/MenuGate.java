package resources.menus.states;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.rendering.Colour;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class MenuGate extends MenuState {
    public MenuGate(StateContext ctx) {
        super(ctx);


        var clickContainer = ctx.ui().new ButtonBuilder()
                .withDimensions(512, 512)
                .withPosition(new Point(0,0), Positioning.FIXED)
                .withAlignment(Alignment.TOP_LEFT)
                .onHoverEnter(container -> {
                    try {
                        synchronized (this) {
                            this.wait(500);
                        }
                    } catch (Exception e) {
                        IO.println("Mouse reporting detection interrupted");
                        ctx.switchTo().accept(ctx.states().mainMenu);
                    }
                    ctx.switchTo().accept(ctx.states().mainMenu);
                })
                .build();

        var mainString = "This game requires terminal mouse support to play";
        var unixString = "It is recommend to use a UNIX-based operating system for best compatibility with the graphics library";
        var helpString = "Some terminals may require you to enable mouse reporting or press SHIFT before reporting begins";
        var mainLabel = ctx.ui().new LabelBuilder<>()
                .withStaticText(mainString)
                .withPosition(new Point(0,-2), Positioning.FIXED)
                .withAlignment(engine_interfaces.objects.Alignment.CENTER)
                .withDimensions(mainString.length(), 1)
                .build();

        var unixLabel = ctx.ui().new LabelBuilder<>()
                .withStaticText(unixString)
                .withPosition(new Point(0,2), Positioning.FIXED)
                .withAlignment(engine_interfaces.objects.Alignment.CENTER)
                .withDimensions(unixString.length(), 1)
                .build();

        var helpLabel = ctx.ui().new LabelBuilder<>()
                .withStaticText(helpString)
                .withPosition(new Point(0,-2), Positioning.FIXED)
                .withAlignment(Alignment.BOTTOM_CENTER)
                .withDimensions(helpString.length(), 1)
                .build();


        show(clickContainer);
        show(mainLabel);
        show(unixLabel);
        show(helpLabel);
    }
}
