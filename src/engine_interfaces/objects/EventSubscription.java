package engine_interfaces.objects;

import java.util.function.BooleanSupplier;

public class EventSubscription{
    public BooleanSupplier isEnabled;
    public EventHandle handle;


    public EventSubscription(BooleanSupplier isEnabled, EventHandle handler) {
        this.isEnabled = isEnabled;
        this.handle = handler;
    }
}