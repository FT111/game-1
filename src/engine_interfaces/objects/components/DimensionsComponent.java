package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

public class DimensionsComponent extends Component {
    public int width;
    public int height;

    public DimensionsComponent(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
