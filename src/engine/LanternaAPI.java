package engine;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.input.MouseAction;
import com.googlecode.lanterna.input.MouseActionType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.*;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import engine_interfaces.objects.MouseEventTypes;
import engine_interfaces.objects.Point;
import engine_interfaces.objects.events.KeyInputEvent;
import engine_interfaces.objects.events.MouseInputEvent;
import engine_interfaces.objects.rendering.GraphicsAPI;
import engine_interfaces.objects.rendering.RenderBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

public class LanternaAPI implements GraphicsAPI {
    Terminal terminal;
    Screen screen;
    private ArrayList<Consumer<KeyInputEvent>> keyInputListeners = new ArrayList<>();
    private ArrayList<Consumer<MouseInputEvent>> mouseInputListeners = new ArrayList<>();

    private MouseEventTypes mapLanternaMouseEventToNative(MouseActionType type) {
        return switch (type) {
            case CLICK_DOWN -> MouseEventTypes.DOWN;
            case CLICK_RELEASE -> MouseEventTypes.UP;
            case DRAG -> MouseEventTypes.DRAG;
            case MOVE -> MouseEventTypes.MOVE;
            case SCROLL_UP -> MouseEventTypes.SCROLL_UP;
            case SCROLL_DOWN -> MouseEventTypes.SCROLL_DOWN;
        };
    }

    public LanternaAPI() throws IOException, InterruptedException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory()
                .setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE)
                .setInitialTerminalSize(new TerminalSize(80, 24));

        terminal = factory.createTerminal();

        screen = new TerminalScreen(terminal);
        terminal.addResizeListener((TerminalResizeListener) (terminal, newSize) -> {
            screen.doResizeIfNecessary();
        });
        screen.setCursorPosition(null);
        screen.startScreen();
        startInputHandling();
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
    public void showWindow() throws IOException {
        screen.stopScreen();
    }

    @Override
    public void hideWindow() throws IOException {
        screen.startScreen();
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

    public void startInputHandling() throws IOException {
        new Thread(() -> {
            while (true) {
                KeyStroke input = null;
                try {
                    input = screen.pollInput();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (input != null) {
                    if (input instanceof MouseAction mouseAction) {
                        MouseInputEvent event = new MouseInputEvent(new Point(mouseAction.getPosition().getColumn(),
                                mouseAction.getPosition().getRow()),
                                mapLanternaMouseEventToNative(mouseAction.getActionType()));

                        for (Consumer<MouseInputEvent> listener : mouseInputListeners) {
                            listener.accept(event);
                        }
                        continue;
                    }

                    if (input.getKeyType() == KeyType.Character) {
                        char character = input.getCharacter();
                        KeyInputEvent event = new KeyInputEvent(character);
                        for (Consumer<KeyInputEvent> listener : keyInputListeners) {
                            listener.accept(event);
                    }
                }
            }
        }}).start();
    }

    @Override
    public void listenForKeyInput(Consumer<KeyInputEvent> callback) throws IOException {
        keyInputListeners.add(callback);
    }

    @Override
    public void listenForMouseInput(Consumer<MouseInputEvent> callback) throws IOException {
        mouseInputListeners.add(callback);
    }
}
