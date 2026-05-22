package game.events;

import engine_interfaces.objects.Event;
import game.ai.GuardAlertness;

/**
 * Emitted when the global guard alertness level changes.
 */
public class GlobalAlertnessChangedEvent extends Event {
    public final GuardAlertness level;

    public GlobalAlertnessChangedEvent(GuardAlertness level) {
        this.level = level;
    }
}

