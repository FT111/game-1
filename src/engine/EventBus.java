package engine;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.EventHandle;
import engine_interfaces.objects.EventSubscription;
import engine_interfaces.objects.EventSubscriptionReceipt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class EventBus {
    private ArrayDeque<Event> pendingEvents = new ArrayDeque<>();
    private ArrayDeque<Event> currentEvents = new ArrayDeque<>();

    private final HashMap<Event, ArrayList<EventSubscription>> subscribers = new HashMap<>();

    public final EventSubscriptionReceipt subscribe(Event eventType, String systemID, EventHandle handler) {
        var subscription = new EventSubscription(systemID, handler);

        if (!subscribers.containsKey(eventType)) {
            subscribers.put(eventType, new ArrayList<>());
        }

        subscribers.get(eventType).add(subscription);

        return new EventSubscriptionReceipt(() -> {subscribers.get(eventType).remove(subscription);});
    }

    protected final void flush() {
        currentEvents = pendingEvents;
        pendingEvents = new ArrayDeque<>();
        currentEvents.forEach(event -> {
            subscribers.get(event).forEach(subscriber -> subscriber.handle().handleEvent(event));
        });
        currentEvents.clear();
    }


}
