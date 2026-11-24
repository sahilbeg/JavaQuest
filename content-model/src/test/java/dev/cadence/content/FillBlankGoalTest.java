package dev.cadence.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.cadence.content.Quiz.FillBlank;
import java.util.List;
import org.junit.jupiter.api.Test;

class FillBlankGoalTest {

    private static FillBlank fill(String goal, String... answers) {
        return new FillBlank("q08", "break", goal, "if (tap == goodTap) { ___; }", List.of(answers), "explained", null);
    }

    @Test
    void aGoalThatSaysWhatTheLineDoesIsAccepted() {
        assertEquals("Leave the loop at once when the rider taps the right card.",
                fill("Leave the loop at once when the rider taps the right card.", "break", "break;").goal());
    }

    @Test
    void aFillBlankWithoutAGoalIsRefused() {
        var error = assertThrows(IllegalArgumentException.class, () -> fill(null, "break"));
        assertTrue(error.getMessage().contains("needs a goal"), error.getMessage());
    }

    @Test
    void aGoalMustNotNameTheAnswerInAnyCase() {
        var error = assertThrows(IllegalArgumentException.class, () -> fill("Break out of the loop.", "break"));
        assertTrue(error.getMessage().contains("gives away"), error.getMessage());
    }

    @Test
    void aWordInsideALongerWordIsNotAGiveaway() {
        fill("Leave the loop at once; the rider's card is a breakthrough.", "break");
    }

    @Test
    void aSymbolAnswerMustNotAppearInTheGoal() {
        assertThrows(IllegalArgumentException.class, () -> fill("Count up with i++ each time.", "i++"));
    }

    @Test
    void aFullStopEndingTheGoalIsNotTheAnswerDot() {
        new FillBlank("q06", "fields", "Set the type field inside this one bike to cargo.", "bike___type = \"cargo\";",
                List.of("."), "explained", null);
    }

    @Test
    void aGoalLongerThan25WordsIsRefused() {
        String longGoal = "word ".repeat(26).strip() + ".";
        var error = assertThrows(IllegalArgumentException.class, () -> fill(longGoal, "break"));
        assertTrue(error.getMessage().contains("26 words"), error.getMessage());
    }

    @Test
    void theGoalReachesThePage() {
        var quiz = new Quiz("t", 1, 80, List.of(fill("Leave the loop at once.", "break")));
        var presented = QuizSession.draw(quiz, 1L).getFirst();
        assertEquals("Leave the loop at once.", presented.goal());
    }
}
