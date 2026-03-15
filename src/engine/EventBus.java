package engine;

import engineInterfaces.objects.Event;

import java.util.ArrayDeque;

public class EventBus {
    private final ArrayDeque<Event> pendingQueue = new ArrayDeque<>();
    private final ArrayDeque<Event> processingQueue = new ArrayDeque<>();


}
