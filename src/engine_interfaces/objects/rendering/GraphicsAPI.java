package engine_interfaces.objects.rendering;

public interface GraphicsAPI
{
    void render(RenderBuffer buffer);
    void clear();

    int getWidth();
    int getHeight();

    void onResize(Runnable callback);
}
