package jq.m00.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WelcomeCardTest {

    private static final List<String> DESIGN = List.of(
            "+---------------------------+",
            "| Welcome to Spoke, rider!  |",
            "| Your first 30 minutes are |",
            "| free. Enjoy the ride.     |",
            "+---------------------------+");

    private static List<String> printed() {
        PrintStream original = System.out;
        var captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true));
        try {
            WelcomeCard.main(new String[0]);
        } finally {
            System.setOut(original);
        }
        return captured.toString().lines().toList();
    }

    @Test
    @DisplayName("border() returns the frame line")
    void border() {
        assertEquals(DESIGN.getFirst(), WelcomeCard.border(), "border() should return +, 27 dashes, then +");
    }

    @Test
    @DisplayName("the card has exactly five lines")
    void fiveLines() {
        assertEquals(5, printed().size(), "main should print 5 lines");
    }

    @Test
    @DisplayName("every line matches Priya's design")
    void matchesDesign() {
        var lines = printed();
        for (int i = 0; i < DESIGN.size(); i++) {
            String actual = i < lines.size() ? lines.get(i) : "(missing)";
            assertEquals(DESIGN.get(i), actual, "line " + (i + 1) + " is different from the design");
        }
    }
}
