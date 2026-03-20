package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;

public class InputEvent extends Event {
    public char key;

    public InputEvent(char key) {
        this.key = key;
    }
}
