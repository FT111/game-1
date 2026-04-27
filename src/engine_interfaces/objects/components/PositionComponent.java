package engine_interfaces.objects.components;

import engine_interfaces.objects.Alignment;
import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.Positioning;

public class PositionComponent extends Component {
    public Point Origin;
    public int zIndex = 0;
    public Positioning positionStrategy = Positioning.ABSOLUTE;
    public Alignment alignment = Alignment.TOP_LEFT;

    public PositionComponent(Point origin, int zIndex, Positioning positionStrategy, Alignment alignment) {
        Origin = origin;
        this.zIndex = zIndex;
        this.positionStrategy = positionStrategy;
    }


    public PositionComponent(Point origin) {
        this.Origin = origin;
    }

    public PositionComponent(Point origin, int zIndex) {
        Origin = origin;
        this.zIndex = zIndex;
    }
}

