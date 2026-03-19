package engine_interfaces.objects.rendering;

public class Cell {
    public Character content;
    public Colour fgColour;
    public Colour bgColour;

    public Cell(Character content, Colour fgColour, Colour bgColour) {
        this.content = content;
        this.fgColour = fgColour;
        this.bgColour = bgColour;
    }

    public Cell(Character content) {
        this(content, null, null);
    }
}
