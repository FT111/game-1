package resources;

import engine.EventBus;
import engine.World;
import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;
import engine_interfaces.objects.System;
import engine_interfaces.objects.events.ButtonClickEvent;

import java.util.HashMap;

public class MenuSystem extends System{
    public HashMap<LayerID, Runnable> buttonCallbacks = new HashMap<>();

    public MenuSystem(EventBus bus) {
        bus.subscribe(ButtonClickEvent.class,"MenuSystem", this::handleButtonClick);
    }

    @Override
    public void update(World world, int tickCount) {

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
