package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

public class VelocityComponent extends Component {
    public int lastMovementTick;

    public double accelerationScaleConstant;
    public int minMovementTickFrequency;
    public double verticalMinMovementFrequencyMultiplier = 1.8; // Multiplier applied to maxMovementFrequency when moving vertically, typically more than one to keep visual movement speed consistent due to the rectangular grid
    public String accelerationFunction;

    public VelocityComponent(double accelerationScaleConstant, int minMovementTickFrequency, String accelerationFunction) {
        this.accelerationScaleConstant = accelerationScaleConstant;
        this.minMovementTickFrequency = minMovementTickFrequency;
        this.accelerationFunction = accelerationFunction;
    }

    public VelocityComponent(int lastMovementTick, double accelerationScaleConstant, int minMovementTickFrequency, String accelerationFunction, double verticalMinMovementFrequencyMultiplier) {
        this.lastMovementTick = lastMovementTick;
        this.accelerationScaleConstant = accelerationScaleConstant;
        this.minMovementTickFrequency = minMovementTickFrequency;
        this.verticalMinMovementFrequencyMultiplier = verticalMinMovementFrequencyMultiplier;
        this.accelerationFunction = accelerationFunction;
    }
}

