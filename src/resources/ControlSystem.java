package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.EventSubscriptionReceipt;
import engine_interfaces.objects.System;
import resources.events.QuitGameEvent;

import java.util.ArrayList;
import java.util.List;

public class ControlSystem extends System {
    private final EventBus bus;
    List<EventSubscriptionReceipt> subscriptions = new ArrayList<>();

    public ControlSystem(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        super.onEnter(world);

        subscriptions.add(bus.subscribe(QuitGameEvent.class, () -> isEnabled, this::handleQuitGame));
    }

    private void handleQuitGame(Event event) {
        java.lang.System.exit(0);
    }
}
