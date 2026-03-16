package engineInterfaces.objects;

@FunctionalInterface
public interface EventHandle {
    public void handleEvent(Event event);
}


