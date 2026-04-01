package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.LayerID;

public class ButtonClickEvent extends Event {
    public LayerID buttonLayerId;

    public ButtonClickEvent(LayerID buttonId) {
        this.buttonLayerId = buttonId;
    }
}