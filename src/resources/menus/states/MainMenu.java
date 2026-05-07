package resources.menus.states;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.rendering.Colour;
import engine_interfaces.objects.components.BackgroundComponent;
import resources.StyledUI;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class MainMenu extends MenuState {
    public MainMenu(StateContext stateContext) {
        super(stateContext);
        String continueText = "Continue";
        var mouseReportingDetectedString = "Mouse reporting detected!";
        var detectedLabel = stateContext.ui().new LabelBuilder<>()
                .withStaticText(mouseReportingDetectedString)
                .withPosition(new Point(0,-2), Positioning.FIXED)
                .withAlignment(Alignment.BOTTOM_CENTER)
                .withDimensions(mouseReportingDetectedString.length(), 1)
                .build();

        String menu = "Menu";
        var label = stateContext.ui().new LabelBuilder<>()
                .withStaticText(menu)
                .withPosition(new Point(0, -2), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(menu.length(), 1)
                .build();

        var continueButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText(continueText)
                .withPosition(new Point(0,2), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(continueText.length(), 1)
                .onClick((btn) -> {
                    hide(detectedLabel);
                    stateContext.switchTo().accept(stateContext.states().gameHud);
                    stateContext.bus().publish(new PopSceneEvent());
                    stateContext.bus().publish(new engine_interfaces.objects.events.PushSceneEvent("Gameplay"));
                })
                .build();

        String save = "Save";
        var saveButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText(save)
                .withPosition(new Point(0,4), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(save.length(), 1)
                .build();

        String load = "Load";
        var loadButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText(load)
                .withPosition(new Point(0,6), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(load.length(), 1)
                .build();

        String quit = "Quit";
        var quitButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText(quit)
                .withPosition(new Point(0,8), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(quit.length(), 1)
                .onClick((btn) -> {
                    stateContext.bus().publish(new resources.events.QuitGameEvent());
                })
                .build();


        show(continueButton);
        show(label);
        show(saveButton);
        show(loadButton);
        show(quitButton);
        show(detectedLabel);

    }

}
