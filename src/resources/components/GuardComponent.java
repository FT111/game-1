package resources.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.Point;
import resources.ai.GuardState;

import java.util.Stack;

public class GuardComponent extends Component {
    GuardState state = GuardState.PATROLLING;
    Stack<Point> mainPathPoints;
    Stack<Point> interpolatedPathPoints;
}
