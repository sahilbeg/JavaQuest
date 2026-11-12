package jq.m01.consoleio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KioskPromptTest {

    private static String runWithInput(String typed) {
        PrintStream original = System.out;
        var captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true));
        try {
            KioskPrompt.run(new Scanner(typed));
        } finally {
            System.setOut(original);
        }
        return captured.toString().replace("\r\n", "\n").strip();
    }

    @Test
    @DisplayName("readMinutes returns the number the rider typed")
    void readMinutes() {
        assertEquals(25, KioskPrompt.readMinutes(new Scanner("25\n")),
                "readMinutes should return 25 when the rider types 25. Use in.nextInt()");
    }

    @Test
    @DisplayName("readStation returns the line without extra spaces")
    void readStation() {
        assertEquals("Old Mill", KioskPrompt.readStation(new Scanner("  Old Mill  \n")),
                "readStation should return \"Old Mill\" for \"  Old Mill  \". Use in.nextLine().trim()");
    }

    @Test
    @DisplayName("run asks the question and prints the fare for 10 minutes")
    void runTenMinutes() {
        assertEquals("How many minutes did you ride?\nFare: $2.5", runWithInput("10\n"),
                "For 10 minutes run should print the question, then Fare: $2.5 (1.00 + 10 * 0.15), each with println");
    }

    @Test
    @DisplayName("run prints the fare for 30 minutes")
    void runThirtyMinutes() {
        assertEquals("How many minutes did you ride?\nFare: $5.5", runWithInput("30\n"),
                "For 30 minutes run should print the question, then Fare: $5.5 (1.00 + 30 * 0.15)");
    }
}
