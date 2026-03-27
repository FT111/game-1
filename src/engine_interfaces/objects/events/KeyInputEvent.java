package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;

public class KeyInputEvent extends Event {
    public char key;
    public boolean isCtrlPressed;
    public boolean isShiftPressed;
    public boolean isAltPressed;

    public KeyInputEvent(char key) {
        this.key = key;
    }

    public KeyInputEvent(char key, boolean isCtrlPressed, boolean isShiftPressed, boolean isAltPressed) {
        this.key = key;
        this.isCtrlPressed = isCtrlPressed;
        this.isShiftPressed = isShiftPressed;
        this.isAltPressed = isAltPressed;
    }
}
