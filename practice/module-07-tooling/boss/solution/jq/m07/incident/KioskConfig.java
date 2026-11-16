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
        Path file = configDir.resolve(FILE_NAME);
        KioskConfig defaults = defaults();
        if (!Files.exists(file)) {
            return defaults;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }
        return new KioskConfig(
                text(properties, "station", defaults.station()),
                number(properties, "cents.per.minute", defaults.centsPerMinute()),
                number(properties, "unlock.cents", defaults.unlockCents()));
    }

    private static String text(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : value.strip();
    }

    private static long number(Properties properties, String key, long fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Long.parseLong(value.strip());
    }
}
