package jq.m07.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KioskTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String... args) {
        return Kiosk.run(args, new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private String out() {
        return out.toString(StandardCharsets.UTF_8).strip();
    }

    private String err() {
        return err.toString(StandardCharsets.UTF_8).strip();
    }

    @Test
    @DisplayName("12 minutes prints the fare and exits with 0")
    void fare() {
        assertEquals(0, run("12"), "a good command exits with code 0");
        assertEquals("Fare for 12 min: $2.80", out(), "100 cents to unlock plus 12 x 15 cents is 280 cents");
    }

    @Test
    @DisplayName("0 minutes costs only the unlock fee")
    void zeroMinutes() {
        assertEquals(0, run("0"), "0 minutes is a valid ride");
        assertEquals("Fare for 0 min: $1.00", out(), "a 0 minute ride costs the 100 cent unlock fee");
    }

    @Test
    @DisplayName("--version prints the build version, which is dev outside a jar")
    void version() {
        assertEquals("dev", BuildInfo.version(), "these checks run from compiled classes, not a jar, so there is no manifest version");
        assertEquals(0, run("--version"), "--version exits with code 0");
        assertEquals("Spoke kiosk dev", out(), "print \"Spoke kiosk \" and then BuildInfo.version()");
    }

    @Test
    @DisplayName("Wrong arguments print the usage line to the error stream and exit with 2")
    void wrongArguments() {
        String[][] cases = {{}, {"abc"}, {"-5"}, {"12", "13"}};
        for (String[] args : cases) {
            out.reset();
            err.reset();
            String shown = String.join(" ", args);
            assertEquals(2, run(args), "\"" + shown + "\" is not a valid command, so the exit code should be 2");
            assertEquals(Kiosk.USAGE, err(), "\"" + shown + "\" should print the usage line to err");
            assertTrue(out().isEmpty(), "\"" + shown + "\" should print nothing to out");
        }
    }
}
