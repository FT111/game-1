package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;

public class PositionComponent extends Component {
    public Point Origin;

    public PositionComponent(Point origin) {
        this.Origin = origin;
    }
}
