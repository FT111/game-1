package engine_interfaces.objects.components.ui;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.ui.SelectionStrategies;

public class HoverComponent extends Component {
    public SelectionStrategies SelectionStrategy;
    public boolean visibilityDependent = true;

    public HoverComponent(SelectionStrategies selectionStrategy) {
        SelectionStrategy = selectionStrategy;
    }
}
