package resources.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;
import resources.ai.GuardState;
import resources.ai.bt.BtNode; // added

import java.util.List;
import java.util.Stack;

public class GuardComponent extends Component {
    public GuardState state = GuardState.PATROLLING;
    public Stack<Point> mainPathPoints = new Stack<>();
    public Stack<Point> interpolatedPathPoints = new Stack<>();

    public BtNode behaviourTree; // added

    public GuardComponent() {
    }

    public GuardComponent(List<Point> mainPathPoints, GuardState state) {
        this.mainPathPoints.addAll(mainPathPoints);
        this.state = state;
    }
}
