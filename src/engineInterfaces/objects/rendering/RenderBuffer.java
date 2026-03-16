package engineInterfaces.objects.rendering;

public class RenderBuffer {
    public Cell[][] cells;
    public int width;
    public int height;

    public RenderBuffer(int width, int height) {
        this.width = width;
        this.height = height;

        cells = new Cell[height][width];
    }
}
