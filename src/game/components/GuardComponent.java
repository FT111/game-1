package game.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;
import game.ai.GuardState;
import game.ai.bt.BtNode; // added

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class GuardComponent extends Component {
    public GuardState state = GuardState.PATROLLING;
    public List<Point> patrolPoints = new ArrayList<>();
    public int patrolIndex = 0;
    public Stack<Point> interpolatedPathPoints = new Stack<>();

    public BtNode behaviourTree;

    public GuardComponent() {
    }

    public GuardComponent(List<Point> patrolPoints, GuardState state) {
        this.patrolPoints.addAll(patrolPoints);
        this.state = state;
    }
}
