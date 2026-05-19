package engine;

import org.junit.jupiter.api.Test;
import testsupport.TestEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EventBusTest {

    @Test
    void publishEnsuresUniqueEventIds() {
        EventBus bus = new EventBus();
        TestEvent first = new TestEvent("first");
        TestEvent second = new TestEvent("second");
        second.eventID = first.eventID;

        bus.publish(first);
        bus.publish(second);

        assertNotEquals(first.eventID, second.eventID);
    }

    @Test
    void flushDeliversToEnabledSubscribers() {
        EventBus bus = new EventBus();
        AtomicInteger calls = new AtomicInteger(0);

        bus.subscribe(TestEvent.class, () -> true, event -> calls.incrementAndGet());
        bus.publish(new TestEvent("go"));

        bus.flush();

        assertEquals(1, calls.get());
    }

    @Test
    void flushSkipsDisabledSubscribers() {
        EventBus bus = new EventBus();
        AtomicInteger calls = new AtomicInteger(0);
        AtomicBoolean enabled = new AtomicBoolean(false);

        bus.subscribe(TestEvent.class, enabled::get, event -> calls.incrementAndGet());
        bus.publish(new TestEvent("nope"));

        bus.flush();

        assertEquals(0, calls.get());
    }

    @Test
    void cancelSubscriptionStopsDelivery() {
        EventBus bus = new EventBus();
        AtomicInteger calls = new AtomicInteger(0);

        var receipt = bus.subscribe(TestEvent.class, () -> true, event -> calls.incrementAndGet());
        receipt.cancel.run();
        bus.publish(new TestEvent("cancelled"));

        bus.flush();

        assertEquals(0, calls.get());
    }
}
