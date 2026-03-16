package engine_interfaces.objects;

@FunctionalInterface
public interface EventHandle {
    public void handleEvent(Event event);
}


