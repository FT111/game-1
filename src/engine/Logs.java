package engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class Logs {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("engine.debug");

    static {
        configureLogger();
    }

    private Logs() {
    }

    public static void log(String message) {
        IO.println(message);
        LOGGER.info(message);
    }

    private static void configureLogger() {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);

        try {
            Path logDirectory = Path.of("logs");
            Files.createDirectories(logDirectory);

            var currentDateTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            FileHandler fileHandler = new FileHandler(logDirectory.resolve("debug_" + currentDateTime + ".log").toString());
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return "[DBG " + record.getMillis() + "] " + record.getMessage() + System.lineSeparator();
                }
            });

            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize filesystem logger", e);
        }
    }
}

