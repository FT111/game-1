package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.events.ButtonClickEvent;
import engine_interfaces.objects.events.KeyInputEvent;
import resources.menus.KeyInputBind;
import resources.menus.MenuState;
import resources.menus.MenuStates;

import java.util.HashMap;
import java.util.Set;

public class MenuSystem extends System{
    public HashMap<LayerID, Runnable> buttonCallbacks = new HashMap<>();
    public HashMap<KeyInputBind, Runnable> keyCallbacks = new HashMap<>();
    private UiBuilders uiBuilders;
    private MenuStates states;
    private MenuState currentMenuState;
    private World world;

    public MenuSystem(EventBus bus, World world) {
        this.world = world;
        this.uiBuilders = new UiBuilders(world);
        this.states = new MenuStates(this::switchState, uiBuilders, bus);

        this.currentMenuState = states.mainMenu;
        bus.subscribe(ButtonClickEvent.class, () -> isEnabled, this::handleButtonClick);
        bus.subscribe(KeyInputEvent.class, () -> isEnabled, this::handleKeyPress);

        switchState(currentMenuState);
    }


    @Override
    public void update(World world, int tickCount) {

    }

    public void switchState(MenuState newState) {
       Set<LayerID> newLayers = newState.getLayers();
        // only hide layers in neither state
        var removedLayers = currentMenuState.getLayers().stream().filter(layer -> !newLayers.contains(layer)).toList();

        // hide current layers
        for (var layer : removedLayers) {
            world.Layers.get(layer).put(VisibilityComponent.class, new VisibilityComponent(false));
        }

        currentMenuState = newState;
        // Clear then set button callbacks to ones defined in state
        buttonCallbacks.clear();
        buttonCallbacks.putAll(newState.getClickBindings());
        keyCallbacks.clear();
        keyCallbacks.putAll(newState.getKeyPressBindings());
        // Set layers to visible
        for (var layer : newLayers) {
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

    private void handleKeyPress(Event event) {
        if (!(event instanceof KeyInputEvent keyInput)) {
            return;
        }


        var callback = keyCallbacks.get(
                new KeyInputBind(keyInput.key, keyInput.isShiftPressed, keyInput.isCtrlPressed, keyInput.isAltPressed)
        );
        if (callback != null) {
            callback.run();
        }
    }
}
