package resources.scenes;

import engine.scenes.Scene;
import engine.systems.UiInteractionSystem;
import resources.MenuSystem;

public class MainMenuScene extends Scene {

    public MainMenuScene(MenuSystem menuSystem, UiInteractionSystem uiSystem) {
        add(menuSystem);
        add(uiSystem);
    }

    @Override
    protected void onEnter() {
        java.lang.System.out.println("Entering Main Menu Scene");
    }
}
