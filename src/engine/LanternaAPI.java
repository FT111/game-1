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
    private static final int DEFAULT_WIDTH = 80;
    private static final int DEFAULT_HEIGHT = 24;

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
        Logs.log("LanternaAPI: constructor start");
        DefaultTerminalFactory factory = new DefaultTerminalFactory()
                .setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE)
                .setInitialTerminalSize(new TerminalSize(80, 24));
//                .setTerminalEmulatorFontConfiguration(new SwingTerminalFontConfiguration(true, AWTTerminalFontConfiguration.BoldMode.NOTHING, new Font("Monospaced", Font.PLAIN, 16)));
        terminal = createTerminalWithFallback(factory);
        Logs.log("LanternaAPI: terminal created using " + terminal.getClass().getSimpleName());

        screen = new TerminalScreen(terminal);
        Logs.log("LanternaAPI: terminal screen created");
        screen.setCursorPosition(null);
        screen.startScreen();
        Logs.log("LanternaAPI: screen started");
        Logs.log("LanternaAPI: initial terminal size " + getWidth() + "x" + getHeight());
        startInputHandling();
        Logs.log("LanternaAPI: input handling started");
        terminal.addResizeListener((TerminalResizeListener) (terminal, newSize) -> {
            screen.doResizeIfNecessary();
        });

    }

    // TODO: add native windows terminal support
    private Terminal createTerminalWithFallback(DefaultTerminalFactory factory) throws IOException {
        try {
            Logs.log("LanternaAPI: trying native terminal backend");
            return factory.createTerminal();
        } catch (Exception nativeInitError) {
            Logs.log("LanternaAPI: native backend failed - " + nativeInitError.getClass().getSimpleName() + ": " + nativeInitError.getMessage());
            try {
                Logs.log("LanternaAPI: trying terminal emulator backend");
                return factory.createHeadlessTerminal();
            } catch (RuntimeException emulatorInitError) {
                Logs.log("LanternaAPI: terminal emulator backend failed - " + emulatorInitError.getClass().getSimpleName() + ": " + emulatorInitError.getMessage());
                Logs.log("LanternaAPI: falling back to swing terminal backend");
                return factory.createSwingTerminal();
            }
        }
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
        screen.startScreen();
    }

    @Override
    public void hideWindow() throws IOException {
        screen.stopScreen();
    }

    @Override
    public int getWidth() {
        TerminalSize size = screen.getTerminalSize();
        if (size == null || size.getColumns() <= 0) {
            return DEFAULT_WIDTH;
        }

        return size.getColumns();
    }

    @Override
    public int getHeight() {
        TerminalSize size = screen.getTerminalSize();
        if (size == null || size.getRows() <= 0) {
            return DEFAULT_HEIGHT;
        }

        return size.getRows();
    }

    @Override
    public Runnable onResize(Runnable callback) {
        TerminalResizeListener listener = (terminal, terminalSize) -> callback.run();
        terminal.addResizeListener(listener);
        return () -> terminal.removeResizeListener(listener);
    }

    public void startInputHandling() throws IOException {
        Logs.log("LanternaAPI: creating input thread");
        Thread inputThread = new Thread(() -> {
            while (true) {
                KeyStroke input = null;
                try {
                    input = screen.readInput();
                } catch (IOException e) {
                    Logs.log("LanternaAPI: input thread read failed - " + e.getMessage());
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
            }
        });
        inputThread.setName("LanternaInputThread");
        inputThread.start();
    }

    @Override
    public Runnable listenForKeyInput(Consumer<KeyInputEvent> callback) throws IOException {
        keyInputListeners.add(callback);
        return () -> keyInputListeners.remove(callback);
    }

    @Override
    public Runnable listenForMouseInput(Consumer<MouseInputEvent> callback) throws IOException {
        mouseInputListeners.add(callback);
        return () -> mouseInputListeners.remove(callback);
    }
}
