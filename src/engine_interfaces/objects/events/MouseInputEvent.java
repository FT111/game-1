package engine_interfaces.objects.events;

import engine_interfaces.objects.Event;
import engine_interfaces.objects.MouseEventTypes;
import engine_interfaces.objects.Point;

public class MouseInputEvent extends Event {
    public Point screenPosition;
    public MouseEventTypes eventType;

    public MouseInputEvent(Point screenPosition, MouseEventTypes event) {
        this.screenPosition = screenPosition;
        this.eventType = event;
    }


}
