package jq.m07.incident;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Kiosk settings, read from kiosk.properties in a config folder. */
public record KioskConfig(String station, long centsPerMinute, long unlockCents) {

    public static final String FILE_NAME = "kiosk.properties";

    public static KioskConfig defaults() {
        return new KioskConfig("Riverside", 15, 100);
    }

    /**
     * Reads FILE_NAME from configDir. A missing file gives the defaults. A missing key gives that key's
     * default. Values may have spaces around them.
     */
    public static KioskConfig load(Path configDir) throws IOException {
        // TODO silver 1: this path only exists on Maya's laptop. Read FILE_NAME inside configDir instead.
        Path file = Path.of("/Users/maya/spoke/kiosk.properties");
        // TODO silver 2: when the file does not exist, return defaults() instead of crashing.
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }
        // TODO silver 3: a missing key falls back to its default, and every value loses its spaces:
        //               "Old Mill  " becomes "Old Mill", and "15 " reads as 15.
        return new KioskConfig(
                properties.getProperty("station"),
                Long.parseLong(properties.getProperty("cents.per.minute")),
                Long.parseLong(properties.getProperty("unlock.cents")));
    }
}
