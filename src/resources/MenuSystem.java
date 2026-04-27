package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.EventSubscriptionReceipt;
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
    private final World world;
    private final EventBus bus;
    private EventSubscriptionReceipt buttonClickSubscription;
    private EventSubscriptionReceipt keyInputSubscription;

    public MenuSystem(EventBus bus, World world) {
        this.bus = bus;
        this.world = world;
    }

    @Override
    public void onEnter(World world) {
        if (states == null) {
            uiBuilders = new UiBuilders(this.world);
            states = new MenuStates(this::switchState, uiBuilders, bus);
            currentMenuState = states.mainMenu;
        }

        buttonClickSubscription = bus.subscribe(ButtonClickEvent.class, () -> isEnabled, this::handleButtonClick);
        keyInputSubscription = bus.subscribe(KeyInputEvent.class, () -> isEnabled, this::handleKeyPress);
        switchState(currentMenuState);
    }

    @Override
    public void onExit(World world) {
        if (buttonClickSubscription != null) {
            buttonClickSubscription.cancel.run();
            buttonClickSubscription = null;
        }

        if (keyInputSubscription != null) {
            keyInputSubscription.cancel.run();
            keyInputSubscription = null;
        }

        if (currentMenuState != null) {
            currentMenuState.getLayers().forEach(layer -> {
                if (this.world.Layers.containsKey(layer)) {
                    this.world.Layers.get(layer).put(VisibilityComponent.class, new VisibilityComponent(false));
                }
            });
        }

        buttonCallbacks.clear();
        keyCallbacks.clear();
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
