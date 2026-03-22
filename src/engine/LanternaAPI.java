package engine;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.SimpleTerminalResizeListener;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

public class LanternaAPI implements GraphicsAPI {
    Terminal terminal;
    Screen screen;

    public LanternaAPI() throws IOException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        terminal = factory.createTerminal();
        terminal.enterPrivateMode();

        screen = new TerminalScreen(terminal);
        terminal.addResizeListener((TerminalResizeListener) (terminal, newSize) -> {
            screen.doResizeIfNecessary();
        });
        screen.doResizeIfNecessary();
        screen.setCursorPosition(null);
        screen.startScreen();
    }

    @Override
    public void render(RenderBuffer buffer) throws IOException {
        screen.clear();
        for (int i = 0; i < buffer.cells.length; i++) {
                for (int j = 0; j < buffer.cells[i].length; j++) {

                    if (buffer.cells[i][j] == null) {
                        continue;
                    }

                    TextColor foregroundColour;
                    if (buffer.cells[i][j].fgColour == null) {
                        foregroundColour = TextColor.ANSI.WHITE;
                    } else {
                        foregroundColour = new TextColor.RGB(buffer.cells[i][j].fgColour.R, buffer.cells[i][j].fgColour.G, buffer.cells[i][j].fgColour.B);
                    }
                    TextColor backgroundColour;
                    if (buffer.cells[i][j].bgColour == null) {
                        backgroundColour = TextColor.ANSI.BLACK;
                    } else {
                        backgroundColour = new TextColor.RGB(buffer.cells[i][j].bgColour.R, buffer.cells[i][j].bgColour.G, buffer.cells[i][j].bgColour.B);
                    }

                    TextCharacter lanternaCell = TextCharacter.fromCharacter(buffer.cells[i][j].content, foregroundColour, backgroundColour)[0];
                    screen.setCharacter(j, i, lanternaCell);
                }
        }
        screen.refresh(Screen.RefreshType.DELTA);
    }

    @Override
    public void clear() {
        screen.clear();
    }

    @Override
    public int getWidth() {
        return screen.getTerminalSize().getColumns();
    }

    @Override
    public int getHeight() {
        return screen.getTerminalSize().getRows();
    }

    @Override
    public void onResize(Runnable callback) {
            terminal.addResizeListener((terminal, terminalSize) -> callback.run());
    }

    @Override
    public void listenForInput(java.util.function.Consumer<Character> callback) throws IOException {
        new Thread(() -> {
            while (true) {
                KeyStroke input = null;
                try {
                    input = screen.pollInput();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (input != null) {
                    callback.accept(input.getCharacter());
                }
            }
        }).start();
    }
}
