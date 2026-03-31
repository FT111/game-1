package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

public class VisibilityComponent extends Component {
    public boolean isVisible;

    public VisibilityComponent(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
