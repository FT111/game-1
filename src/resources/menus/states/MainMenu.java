package resources.menus.states;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;
import engine_interfaces.objects.rendering.Colour;
import engine_interfaces.objects.components.BackgroundComponent;
import resources.StyledUI;
import resources.menus.MenuState;
import resources.menus.StateContext;

public class MainMenu extends MenuState {
    public MainMenu(StateContext stateContext) {
        super(stateContext);
        String continueText = "Continue";

        var label = stateContext.ui().new LabelBuilder<>()
                .withStaticText("Menu")
                .withPosition(new Point(0, -2), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions("Menu".length(), 1)
                .build();

        var continueButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText(continueText)
                .withPosition(new Point(0,2), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withDimensions(continueText.length(), 1)
                .onClick((btn) -> {
                    stateContext.switchTo().accept(stateContext.states().gameHud);
                    stateContext.bus().publish(new engine_interfaces.objects.events.SwitchSceneEvent("Gameplay"));
                })
                .build();

        var saveButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText("Save")
                .withPosition(new Point(0,4), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withBackground(new Colour(120, 120, 120), null, -1)
                .withDimensions("Save".length(), 1)
                .build();

        var loadButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText("Load")
                .withPosition(new Point(0,6), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withBackground(new Colour(120, 120, 120), null, -1)
                .withDimensions("Load".length(), 1)
                .build();

        var quitButton = StyledUI.menuButton(stateContext.ui())
                .withStaticText("Quit")
                .withPosition(new Point(0,8), Positioning.FIXED)
                .withAlignment(Alignment.CENTER)
                .withBackground(new Colour(120, 120, 120), null, -1)
                .withDimensions("Quit".length(), 1)
                .onClick((btn) -> {
                    stateContext.bus().publish(new resources.events.QuitGameEvent());
                })
                .build();

        var mouseReportingDetectedString = "Mouse reporting detected!";
        var detectedLabel = stateContext.ui().new LabelBuilder<>()
                .withStaticText(mouseReportingDetectedString)
                .withPosition(new Point(0,-2), Positioning.FIXED)
                .withAlignment(Alignment.BOTTOM_CENTER)
                .withDimensions(mouseReportingDetectedString.length(), 1)
                .build();

        show(continueButton);
        show(label);
        show(saveButton);
        show(loadButton);
        show(quitButton);
        show(detectedLabel);

    }

}
