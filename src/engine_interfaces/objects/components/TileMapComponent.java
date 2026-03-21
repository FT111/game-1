package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

public class TileMapComponent extends Component {
    public String resourceId;
    public String assetId;

    public String originPosition;

    public boolean isStatic;
    public boolean isVisible;

    public int width;
    public int height;

    public TileMapComponent(String resourceId, String assetId, String originPosition, boolean isStatic, boolean isVisible, int width, int height) {
        this.resourceId = resourceId;
        this.assetId = assetId;
        this.originPosition = originPosition;
        this.isStatic = isStatic;
        this.isVisible = isVisible;
        this.width = width;
        this.height = height;
    }
}
