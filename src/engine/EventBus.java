package engine;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.EventHandle;
import engine_interfaces.objects.EventSubscription;
import engine_interfaces.objects.EventSubscriptionReceipt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EventBus {
    private ConcurrentLinkedDeque<Event> pendingEvents = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<Event> currentEvents = new ConcurrentLinkedDeque<>();

    private final HashMap<Class<? extends Event>, ArrayList<EventSubscription>> subscribers = new HashMap<>();

    public final EventSubscriptionReceipt subscribe(Class<? extends Event> eventType, String systemID, EventHandle handler) {
        var subscription = new EventSubscription(systemID, handler);


        if (!subscribers.containsKey(eventType)) {
            subscribers.put(eventType, new ArrayList<>());
        }

        subscribers.get(eventType).add(subscription);

        return new EventSubscriptionReceipt(() -> {subscribers.get(eventType).remove(subscription);});
    }

    public final void publish(Event event) {
        pendingEvents.add(event);
        IO.println("Published event: " + event.getClass().getSimpleName());
    }

    protected final void flush() {
        if (pendingEvents.isEmpty()) return;

        currentEvents = pendingEvents;
        pendingEvents = new ConcurrentLinkedDeque<>();

//        // Process events in a new thread to avoid blocking the main game loop
//        new Thread(() -> {
            while (!currentEvents.isEmpty()) {
                var event = currentEvents.poll();
                subscribers.getOrDefault(event.getClass(), new ArrayList<>()).forEach(subscriber -> subscriber.handle().handleEvent(event));
            }
//        }).start();
//        currentEvents.forEach(event -> {
//            subscribers.get(event).forEach(subscriber -> subscriber.handle().handleEvent(event));
//        });
    }


}
