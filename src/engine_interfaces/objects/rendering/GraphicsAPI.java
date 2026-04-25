package engine_interfaces.objects.rendering;

import engine_interfaces.objects.events.KeyInputEvent;
import engine_interfaces.objects.events.MouseInputEvent;

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

    Runnable onResize(Runnable callback);

    Runnable listenForKeyInput(Consumer<KeyInputEvent> callback) throws IOException;
    Runnable listenForMouseInput(Consumer<MouseInputEvent> callback) throws IOException;
}
