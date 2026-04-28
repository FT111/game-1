package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.rendering.Colour;

public class BackgroundComponent extends Component {
    public Colour bgColour;
    public Character fillChar; // character to use for the background fill (defaults to space if null)
    public boolean enabled;
    public int zIndex = 0;

    public BackgroundComponent(Colour bgColour) {
        this(bgColour, null, true);
    }

    public BackgroundComponent(Colour bgColour, Character fillChar, boolean enabled) {
        this.bgColour = bgColour;
        this.fillChar = fillChar;
        this.enabled = enabled;
    }

    public BackgroundComponent(Colour bgColour, Character fillChar, boolean enabled, int zIndex) {
        this.bgColour = bgColour;
        this.fillChar = fillChar;
        this.enabled = enabled;
        this.zIndex = zIndex;
    }
}

