package engine_interfaces.objects.components;

import engine_interfaces.objects.Component;
import engine_interfaces.objects.rendering.Colour;

public class RenderableComponent extends Component {
    public char Char;
    public Colour fgColour;
    public Colour bgColour;

    public boolean isVisible;

    public RenderableComponent(char Char, Colour fgColour, Colour bgColour, boolean isVisible) {
        this.Char = Char;
        this.fgColour = fgColour;
        this.bgColour = bgColour;
        this.isVisible = isVisible;
    }
}
