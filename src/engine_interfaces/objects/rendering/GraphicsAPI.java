package engine_interfaces.objects.rendering;

import java.io.IOException;
import java.util.function.Consumer;

public interface GraphicsAPI
{
    void render(RenderBuffer buffer) throws IOException;
    void clear();
    void showWindow() throws IOException;
    void hideWindow() throws IOException;

    int getWidth();
    int getHeight();

    void onResize(Runnable callback);

    void listenForInput(Consumer<Character> callback) throws IOException;
}
