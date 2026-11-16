package jq.m07.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class KioskConfigSilverTest {

    @TempDir
    Path configDir;

    @Test
    @DisplayName("Silver: the config is read from the folder it is given")
    void readsTheGivenFolder() throws IOException {
        Files.writeString(configDir.resolve("kiosk.properties"), """
                station=Harbor
                cents.per.minute=20
                unlock.cents=120
                """);
        assertEquals(new KioskConfig("Harbor", 20, 120), KioskConfig.load(configDir),
                "load(configDir) should read kiosk.properties inside configDir, not a path on Maya's laptop");
    }

    @Test
    @DisplayName("Silver: with no config file, the kiosk uses the defaults")
    void missingFileGivesDefaults() throws IOException {
        assertEquals(KioskConfig.defaults(), KioskConfig.load(configDir),
                "a fresh CI machine has no kiosk.properties, so load should return defaults() instead of crashing");
    }

    @Test
    @DisplayName("Silver: missing keys get their default, and spaces around values are ignored")
    void missingKeysAndSpaces() throws IOException {
        Files.writeString(configDir.resolve("kiosk.properties"), "station = Old Mill  \ncents.per.minute = 18 \n");
        assertEquals(new KioskConfig("Old Mill", 18, 100), KioskConfig.load(configDir),
                "\"Old Mill  \" should become \"Old Mill\", \"18 \" should read as 18, and the missing unlock.cents should be 100");
    }
}
