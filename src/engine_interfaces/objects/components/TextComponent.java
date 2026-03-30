package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;

public class TextComponent extends Component {
    // either the text to display, or an asset ID to look up the text to display
    public String text;

    public String resourceId;
    public String assetId;

    public int maxWidth;
    public int maxHeight;

    public TextComponent(String text, int maxWidth) {
        this.text = text;
        this.maxWidth = maxWidth;
    }

    public TextComponent(String resourceId, String assetId, int maxWidth) {
        this.resourceId = resourceId;
        this.assetId = assetId;
        this.maxWidth = maxWidth;
    }

    public TextComponent(String text, String resourceId, String assetId, int maxHeight, int maxWidth) {
        this.text = text;
        this.resourceId = resourceId;
        this.assetId = assetId;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    public TextComponent(String text) {
        this.text = text;
    }
}
