package jq.m05.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidatorTest {

    record PlainSettings(String stationName, int docks) {
    }

    @Test
    @DisplayName("Valid kiosk settings give no messages")
    void validSettings() {
        assertEquals(List.of(), Validator.validate(new KioskSettings("Harbor", 12, "en")),
                "Harbor with 12 docks and language en breaks no rule, so the list should be empty");
    }

    @Test
    @DisplayName("A blank station name is reported")
    void blankStationName() {
        assertEquals(List.of("stationName must not be blank"),
                Validator.validate(new KioskSettings("   ", 12, "en")),
                "stationName has @NotBlank and holds only spaces. Check that @NotBlank has @Retention(RUNTIME)"
                        + " and that validate uses isBlank()");
    }

    @Test
    @DisplayName("A dock count outside 1 to 12 is reported")
    void docksOutOfRange() {
        assertEquals(List.of("docks must be between 1 and 12"),
                Validator.validate(new KioskSettings("Uptown", 40, "en")),
                "docks has @Range(min = 1, max = 12) and holds 40. Check that @Range has @Retention(RUNTIME)"
                        + " and that validate reads range.min() and range.max()");
        assertEquals(List.of("docks must be between 1 and 12"),
                Validator.validate(new KioskSettings("Uptown", 0, "en")),
                "0 docks is below the minimum of 1, so it should be reported too");
    }

    @Test
    @DisplayName("Two problems give two messages in field order")
    void bothProblems() {
        assertEquals(List.of("stationName must not be blank", "docks must be between 1 and 12"),
                Validator.validate(new KioskSettings("", 13, "en")),
                "Both rules fail here. The messages should follow the field order: stationName first, then docks");
    }

    @Test
    @DisplayName("A class with no annotations gives no messages")
    void noAnnotations() {
        assertEquals(List.of(), Validator.validate(new PlainSettings("", 99)),
                "PlainSettings has no @NotBlank or @Range, so validate should not report anything");
    }
}
