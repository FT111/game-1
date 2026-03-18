package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;

public class CameraComponent extends Component {
    public Point Origin;
    public int viewWidth;
    public int viewHeight;
    public boolean isActive;
}
