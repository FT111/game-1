package engine.systems;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.System;
import engine_interfaces.objects.rendering.GraphicsAPI;

import java.io.IOException;

public class InputHandlerSystem extends System {
    private final GraphicsAPI api;
    private final EventBus bus;
    private Runnable keyInputSubscriptionCancel;
    private Runnable mouseInputSubscriptionCancel;

    public InputHandlerSystem(GraphicsAPI api, EventBus bus) {
        this.api = api;
        this.bus = bus;
    }

    @Override
    public void onEnter(World world) {
        try {
            keyInputSubscriptionCancel = api.listenForKeyInput(bus::publish);
            mouseInputSubscriptionCancel = api.listenForMouseInput(bus::publish);
        } catch (IOException e) {
            throw new RuntimeException("Failed to register input listeners", e);
        }
    }

    @Override
    public void onExit(World world) {
        if (keyInputSubscriptionCancel != null) {
            keyInputSubscriptionCancel.run();
            keyInputSubscriptionCancel = null;
        }

        if (mouseInputSubscriptionCancel != null) {
            mouseInputSubscriptionCancel.run();
            mouseInputSubscriptionCancel = null;
        }
    }

    @Override
    public void update(World world, int tickCount) {

    }
}
