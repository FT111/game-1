package game;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.*;
import engine_interfaces.objects.System;
import engine_interfaces.objects.components.VisibilityComponent;
import engine_interfaces.objects.events.ButtonClickEvent;
import engine_interfaces.objects.events.KeyInputEvent;
import engine_interfaces.objects.events.LayerHoverEvent;
import engine_interfaces.objects.events.LayerHoverExitEvent;
import game.menus.KeyInputBind;
import game.menus.MenuState;
import game.menus.MenuStates;
import game.events.SwitchMenuStateEvent;

import java.util.*;
import java.util.function.Consumer;

public class MenuSystem extends System{
    public HashMap<LayerID, Consumer<Map<Class<? extends Component>, Component>>> buttonCallbacks = new HashMap<>();
    public HashMap<LayerID, Consumer<Map<Class<? extends Component>, Component>>> hoverCallbacks = new HashMap<>();
    public HashMap<LayerID, Consumer<Map<Class<? extends Component>, Component>>> hoverExitCallbacks = new HashMap<>();
    public HashMap<KeyInputBind, Runnable> keyCallbacks = new HashMap<>();
    private MenuStates states;
    private MenuState currentMenuState;
    private final World world;
    private final EventBus bus;
    private final List<EventSubscriptionReceipt> subscriptions = new ArrayList<>();

    public MenuSystem(EventBus bus, World world) {
        this.bus = bus;
        this.world = world;
    }

    @Override
    public void onEnter(World world) {
        if (states == null) {
            UiBuilders uiBuilders = new UiBuilders(this.world);
            states = new MenuStates(this::switchState, uiBuilders, bus, this.world, layerId -> this.world.Layers.get(layerId));
            currentMenuState = states.menuGate;
        }

        subscriptions.add(bus.subscribe(ButtonClickEvent.class, () -> isEnabled, this::handleButtonClick));
        subscriptions.add(bus.subscribe(LayerHoverEvent.class, () -> isEnabled, this::handleHover));
        subscriptions.add(bus.subscribe(LayerHoverExitEvent.class, () -> isEnabled, this::handleHoverExit));
        subscriptions.add(bus.subscribe(KeyInputEvent.class, () -> isEnabled, this::handleKeyPress));
        // Handle menu state switch events
        subscriptions.add(bus.subscribe(SwitchMenuStateEvent.class, () -> isEnabled, this::handleSwitchMenuState));
        switchState(currentMenuState);
    }

    @Override
    public void onExit(World world) {
        subscriptions.forEach(eventSubscriptionReceipt -> {eventSubscriptionReceipt.cancel.run();});

        if (currentMenuState != null) {
            currentMenuState.getLayers().forEach(layer -> {
                if (this.world.Layers.containsKey(layer)) {
                    this.world.Layers.get(layer).put(VisibilityComponent.class, new VisibilityComponent(false));
                }
            });
        }

        buttonCallbacks.clear();
        hoverCallbacks.clear();
        hoverExitCallbacks.clear();
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

        currentMenuState.onLayerShown = null;

        currentMenuState = newState;
        // Clear then set button callbacks to ones defined in state
        buttonCallbacks.clear();
        buttonCallbacks.putAll(newState.getClickBindings());
        hoverCallbacks.clear();
        hoverCallbacks.putAll(newState.getHoverEnterBindings());
        hoverExitCallbacks.clear();
        hoverExitCallbacks.putAll(newState.getHoverExitBindings());
        keyCallbacks.clear();
        keyCallbacks.putAll(newState.getKeyPressBindings());

        newState.onLayerShown = (LayerID layer) ->  world.Layers.get(layer).put(VisibilityComponent.class, new VisibilityComponent(true));
        newState.onLayerHidden = (LayerID layer) -> world.Layers.get(layer).put(VisibilityComponent.class, new VisibilityComponent(false));

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
            callback.accept(world.Layers.get(buttonClick.buttonLayerId));
        }
    }

    private void handleHover(Event event) {
        if (!(event instanceof LayerHoverEvent hoverEvent)) {
            return;
        }

        var callback = hoverCallbacks.get(hoverEvent.layerId);
        if (callback != null) {
            callback.accept(world.Layers.get(hoverEvent.layerId));
        }
    }

    private void handleHoverExit(Event event) {
        if (!(event instanceof LayerHoverExitEvent hoverExitEvent)) {
            return;
        }

        var callback = hoverExitCallbacks.get(hoverExitEvent.layerId);
        if (callback != null) {
            callback.accept(world.Layers.get(hoverExitEvent.layerId));
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

    private void handleSwitchMenuState(Event event) {
        if (!(event instanceof SwitchMenuStateEvent switchEvent)) {
            return;
        }

        MenuState targetState = getMenuStateByName(switchEvent.menuStateName);
        if (targetState != null) {
            switchState(targetState);
        }
    }

    private MenuState getMenuStateByName(String stateName) {
        return switch (stateName) {
            case "gameHud" -> states.gameHud;
            case "mainMenu" -> states.mainMenu;
            case "caughtMenu" -> states.caughtMenu;
            case "completionMenu" -> states.completionMenu;
            case "menuGate" -> states.menuGate;
            default -> null;
        };
    }
}
