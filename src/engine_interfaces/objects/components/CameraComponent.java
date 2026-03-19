package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;

public class CameraComponent extends Component {
    public int viewWidth;
    public int viewHeight;
    public boolean isActive;

    public CameraComponent(int viewWidth, int viewHeight, boolean isActive) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.isActive = isActive;
    }
}
