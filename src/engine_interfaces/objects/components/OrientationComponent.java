package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

public class OrientationComponent extends Component {
    public int facingAngle; // Angle in degrees, where 0 is to the right, 90 is down, 180 is left, and 270 is up

    public OrientationComponent(int facingAngle) {
        this.facingAngle = facingAngle;
    }
}
