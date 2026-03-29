package engine_interfaces.objects.rendering;

public class Cell {
    public Character content;
    public Colour fgColour;
    public Colour bgColour;

    public int zIndex; // used by the renderer to check when to overwrite a cell

    public Cell(Character content, Colour fgColour, Colour bgColour, int zIndex) {
        this.content = content;
        this.fgColour = fgColour;
        this.bgColour = bgColour;
        this.zIndex = zIndex;
    }

    public Cell(Character content) {
        this(content, null, null, 0);
    }
    public Cell(Character content, int zIndex) {this(content, null, null, zIndex);}
    public Cell(Character content, Colour fgColour, Colour bgColour) {
        this(content, fgColour, bgColour, 0);
    }
}
