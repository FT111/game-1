package engine;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

public class LanternaAPI implements GraphicsAPI {
    SwingTerminalFrame terminal;
    Screen screen;

    public LanternaAPI() throws IOException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        terminal = factory.createSwingTerminal();
        screen = new TerminalScreen(terminal);

        terminal.createBufferStrategy(1);
        terminal.setVisible(true);
    }

    @Override
    public void render(RenderBuffer buffer) {
        for (int i = 0; i < buffer.cells.length; i++) {
                for (int j = 0; j < buffer.cells[i].length; j++) {

                }
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void onResize(Runnable callback) {

    }
}
