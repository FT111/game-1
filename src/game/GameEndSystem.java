package game;

import engine.Engine;
import engine.EventBus;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.events.PushSceneEvent;
import game.events.PlayerCaughtEvent;
import game.events.LevelCompletedEvent;
import game.events.SwitchMenuStateEvent;

/**
 * Handles game end conditions - when the player is caught or completes the level.
 * Transitions to main menu and switches to the appropriate menu state.
 */
public class GameEndSystem extends System {
    private final EventBus bus;
    private boolean endEventHandled = false;

    public GameEndSystem(Engine engine) {
        this.bus = engine.EventBus;
    }

    @Override
    public void onEnter(engine.World world) {
        super.onEnter(world);
        bus.subscribe(PlayerCaughtEvent.class, () -> isEnabled, event -> handleGameEnd("caughtMenu"));
        bus.subscribe(LevelCompletedEvent.class, () -> isEnabled, event -> handleGameEnd("completionMenu"));
    }

    private void handleGameEnd(String menuState) {
        if (!endEventHandled) {
            bus.publish(new PopSceneEvent());
            bus.publish(new PushSceneEvent("MainMenu"));
            bus.publish(new SwitchMenuStateEvent(menuState));
            endEventHandled = true;
        }
    }
}
