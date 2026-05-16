package resources;

import engine.Engine;
import engine.EventBus;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.PopSceneEvent;
import engine_interfaces.objects.events.PushSceneEvent;
import engine_interfaces.objects.events.SwitchSceneEvent;
import resources.events.PlayerCaughtEvent;
import resources.events.ShowCaughtMenuEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for PlayerCaughtEvent and pauses gameplay systems for ~1 second
 * then publishes a ShowCaughtMenuEvent so the UI can switch to a caught screen.
 */
public class CaughtSystem extends System {
    public final EventBus bus;

    public CaughtSystem(Engine engine) {
        this.bus = engine.EventBus;
    }

    @Override
    public void onEnter(engine.World world) {
        super.onEnter(world);

        // Subscribe to the existing PlayerCaughtEvent
        bus.subscribe(PlayerCaughtEvent.class, () -> isEnabled, event -> {
            bus.publish(new PopSceneEvent());
            bus.publish(new PushSceneEvent("MainMenu"));
            bus.publish(new ShowCaughtMenuEvent());
        });
    }

}


