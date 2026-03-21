package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

public class VelocityComponent extends Component {
    public int lastMovementTick;

    public double  accelerationScaleConstant;
    public int  maxMovementFrequency;
    public int baseMovementFrequency;
    public String accelerationFunction;

    public VelocityComponent(double accelerationScaleConstant, int maxMovementFrequency, int baseMovementFrequency, String accelerationFunction) {
        this.accelerationScaleConstant = accelerationScaleConstant;
        this.maxMovementFrequency = maxMovementFrequency;
        this.accelerationFunction = accelerationFunction;
        this.baseMovementFrequency = baseMovementFrequency;
    }
}
