package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;

public class PositionComponent extends Component {
    public Point Origin;
    public int zIndex = 0;
    public boolean isStatic = false; // Switches the origin from world position to screen position

    public PositionComponent(Point origin) {
        this.Origin = origin;
    }

    public PositionComponent(Point origin, int zIndex) {
        Origin = origin;
        this.zIndex = zIndex;
    }

    public PositionComponent(Point origin, int zIndex, boolean isStatic) {
        Origin = origin;
        this.zIndex = zIndex;
        this.isStatic = isStatic;
    }
}

