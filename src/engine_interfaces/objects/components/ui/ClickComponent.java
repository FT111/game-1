package engine_interfaces.objects.components.ui;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.ui.SelectionStrategies;

public class ClickComponent extends Component {
    public SelectionStrategies SelectionStrategy;
    public boolean visibilityDependent = true;

    public ClickComponent(SelectionStrategies selectionStrategy, boolean visibilityDependent) {
        SelectionStrategy = selectionStrategy;
        this.visibilityDependent = visibilityDependent;
    }

    public ClickComponent(SelectionStrategies selectionStrategy) {
        SelectionStrategy = selectionStrategy;
    }
}
