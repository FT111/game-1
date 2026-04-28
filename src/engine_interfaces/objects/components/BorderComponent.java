package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.rendering.Colour;

public class BorderComponent extends Component {
    public Character horizontalChar;
    public Character verticalChar;
    public Character cornerChar;
    public Colour fgColour;
    public boolean enabled;
    public int zIndex = 0;
    public int thickness;

    public BorderComponent(Character horizontalChar, Character verticalChar, Character cornerChar, Colour fgColour) {
        this(horizontalChar, verticalChar, cornerChar, fgColour, true, 1);
    }

    public BorderComponent(Character horizontalChar, Character verticalChar, Character cornerChar, Colour fgColour, boolean enabled, int thickness) {
        this.horizontalChar = horizontalChar;
        this.verticalChar = verticalChar;
        this.cornerChar = cornerChar;
        this.fgColour = fgColour;
        this.enabled = enabled;
        this.thickness = Math.max(1, thickness);
    }
}

