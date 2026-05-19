package testsupport;

import engine_interfaces.objects.Event;

public class TestEvent extends Event {
    public final String name;

    public TestEvent(String name) {
        this.name = name;
    }
}
