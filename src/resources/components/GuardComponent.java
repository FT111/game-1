package resources.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;
import resources.ai.GuardState;

import java.util.List;
import java.util.Stack;

public class GuardComponent extends Component {
    public GuardState state = GuardState.PATROLLING;
    public Stack<Point> mainPathPoints;
    public Stack<Point> interpolatedPathPoints;

    public GuardComponent() {
    }

    public GuardComponent(List<Point> mainPathPoints, GuardState state) {
        this.mainPathPoints = new Stack<>();
        this.mainPathPoints.addAll(mainPathPoints);
        this.state = state;
    }
}
