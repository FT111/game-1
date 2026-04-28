package engine_interfaces.objects.components.ui;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.ui.SelectionStrategies;

public class ClickComponent extends Component {
    public SelectionStrategies SelectionStrategy;

    public ClickComponent(SelectionStrategies selectionStrategy) {
        SelectionStrategy = selectionStrategy;
    }
}
