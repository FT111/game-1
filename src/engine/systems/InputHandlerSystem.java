package engine.systems;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.KeyInputEvent;
import engine_interfaces.objects.rendering.GraphicsAPI;

import java.io.IOException;

public class InputHandlerSystem extends System {
    private GraphicsAPI Api;

    public InputHandlerSystem(GraphicsAPI Api, EventBus Bus) throws IOException {
        this.Api = Api;

        Api.listenForKeyInput(Bus::publish);
        Api.listenForMouseInput(Bus::publish);
    }

    @Override
    public void update(World world, int tickCount) {

    }
}
