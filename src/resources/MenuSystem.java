package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.events.ButtonClickEvent;
import resources.menus.MenuState;
import resources.menus.MenuStates;
import resources.menus.states.MainMenu;

import java.util.HashMap;

public class MenuSystem extends System{
    public HashMap<LayerID, Runnable> buttonCallbacks = new HashMap<>();
    private UiBuilders uiBuilders;
    private MenuStates states;
    private MenuState currentMenuState;
    private World world;

    public MenuSystem(EventBus bus, World world) {
        this.world = world;
        this.uiBuilders = new UiBuilders(world);
        this.states = new MenuStates(this::switchState, uiBuilders);

        this.currentMenuState = states.mainMenu;
        bus.subscribe(ButtonClickEvent.class,"MenuSystem", this::handleButtonClick);

        switchState(currentMenuState);
    }


    @Override
    public void update(World world, int tickCount) {

    }

    protected void switchState(MenuState newState) {
        currentMenuState = newState;
        // Clear then set button callbacks to ones defined in state
        buttonCallbacks.clear();
        buttonCallbacks.putAll(newState.getBindings());
        // Set layers to visible
        for (var layer : newState.getLayers()) {
            world.Layers.get(layer).put(VisibilityComponent.class, new VisibilityComponent(true));
        }
    }

    private void handleButtonClick(Event event) {
        if (!(event instanceof ButtonClickEvent buttonClick)) {
            return;
        }

        var callback = buttonCallbacks.get(buttonClick.buttonLayerId);
        if (callback != null) {
            callback.run();
        }
    }
}
