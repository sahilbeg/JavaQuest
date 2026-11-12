package dev.javaquest.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.javaquest.content.Quiz.FillBlank;
import dev.javaquest.content.Quiz.Mcq;
import dev.javaquest.content.Quiz.McqVariant;
import dev.javaquest.content.Quiz.Order;
import dev.javaquest.content.Quiz.PredictOutput;
import dev.javaquest.content.Quiz.SpotTheBug;
import dev.javaquest.content.QuizSession.Presented;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class QuizSessionTest {

    private static final Mcq MCQ = new Mcq("mcq", "types", null, null, null, List.of(
            new McqVariant("Whole number type?", List.of("int", "String", "boolean"), 0),
            new McqVariant("Text type?", List.of("int", "String", "boolean"), 1)), "explained", "## Concept");
    private static final FillBlank FILL = new FillBlank("fill", "printing", "System.out.___(\"Hi\");",
            List.of("println", "print"), "explained", null);
    private static final PredictOutput PREDICT = new PredictOutput("predict", "printing", null,
            "System.out.println(1);\nSystem.out.println(2);", "1\n2", "explained", null);
    private static final Order ORDER = new Order("order", "structure", "Order them",
            List.of("first", "second", "third", "fourth"), "explained", null);
    private static final SpotTheBug BUG = new SpotTheBug("bug", "syntax", null, "a\nb\nc", 2, "explained", null);
    private static final Quiz QUIZ = new Quiz("m/t", 5, 80, List.of(MCQ, FILL, PREDICT, ORDER, BUG));

    @Test
    void theSameSeedAlwaysShowsTheSameAttempt() {
        assertEquals(QuizSession.draw(QUIZ, 42), QuizSession.draw(QUIZ, 42));
    }

    @Test
    void differentSeedsShowDifferentVariantsAndOrders() {
        var attempts = LongStream.range(0, 20).mapToObj(seed -> QuizSession.draw(QUIZ, seed)).toList();
        assertTrue(new HashSet<>(attempts).size() > 1);
        var mcqPrompts = new HashSet<String>();
        attempts.forEach(a -> a.stream().filter(p -> p.id().equals("mcq")).forEach(p -> mcqPrompts.add(p.prompt())));
        assertEquals(2, mcqPrompts.size(), "both mcq variants should appear across seeds");
    }

    @Test
    void drawCoversEveryConceptBeforeRepeatingOne() {
        var quiz = new Quiz("m/t", 4, 80, List.of(MCQ, FILL, PREDICT, ORDER, BUG));
        for (long seed = 0; seed < 50; seed++) {
            var concepts = QuizSession.pick(quiz, seed).stream().map(Quiz.Question::concept).toList();
            assertEquals(4, new HashSet<>(concepts).size(), "seed " + seed + " drew " + concepts);
        }
    }

    @Test
    void orderItemsAreNeverShownAlreadySolved() {
        for (long seed = 0; seed < 50; seed++) {
            Presented order = find(QuizSession.draw(QUIZ, seed), "order");
            assertNotEquals(ORDER.items(), order.items(), "seed " + seed);
        }
    }

    @Test
    void correctAnswersScoreFullMarks() {
        for (long seed = 0; seed < 20; seed++) {
            var result = QuizSession.grade(QUIZ, seed, correctAnswers(QuizSession.draw(QUIZ, seed)));
            assertEquals(100, result.scorePercent(), "seed " + seed + ": " + result);
            assertTrue(result.passed());
            assertTrue(result.weakConcepts().isEmpty());
        }
    }

    @Test
    void wrongAnswersFailAndNameTheWeakConcepts() {
        var presented = QuizSession.draw(QUIZ, 7);
        var answers = correctAnswers(presented);
        answers.put("bug", 3);
        answers.put("fill", "printline");

        var result = QuizSession.grade(QUIZ, 7, answers);

        assertEquals(60, result.scorePercent());
        assertFalse(result.passed());
        assertEquals(List.of("syntax", "printing").stream().sorted().toList(), result.weakConcepts().stream().sorted().toList());
    }

    @Test
    void missingAnswersCountAsWrong() {
        assertEquals(0, QuizSession.grade(QUIZ, 1, Map.of()).scorePercent());
    }

    @Test
    void predictedOutputIgnoresTrailingSpacesAndWindowsLineEnds() {
        var answers = correctAnswers(QuizSession.draw(QUIZ, 3));
        answers.put("predict", "1  \r\n2\r\n\r\n");
        assertTrue(find(QuizSession.grade(QUIZ, 3, answers).questions(), "predict").correct());
    }

    @Test
    void mcqAnswerIndexMustNotBeOutOfRange() {
        var answers = correctAnswers(QuizSession.draw(QUIZ, 3));
        answers.put("mcq", 9);
        assertFalse(find(QuizSession.grade(QUIZ, 3, answers).questions(), "mcq").correct());
    }

    @Test
    void invalidQuestionsAreRejectedWithTheReason() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> new SpotTheBug("b", "c", null, "one line", 4, "e", null));
        assertTrue(error.getMessage().contains("bugLine"));
        assertThrows(IllegalArgumentException.class, () -> new Quiz("m/t", 6, 80, List.of(MCQ)));
    }

    private static Map<String, Object> correctAnswers(List<Presented> presented) {
        var answers = new HashMap<String, Object>();
        for (Presented p : presented) {
            switch (p.id()) {
                case "mcq" -> {
                    String expected = p.prompt().startsWith("Whole") ? "int" : "String";
                    answers.put("mcq", p.choices().indexOf(expected));
                }
                case "fill" -> answers.put("fill", " println ");
                case "predict" -> answers.put("predict", "1\n2");
                case "order" -> answers.put("order",
                        ORDER.items().stream().map(item -> p.items().indexOf(item)).toList());
                case "bug" -> answers.put("bug", 2);
                default -> throw new IllegalStateException(p.id());
            }
        }
        return answers;
    }

    private static <T> T find(List<T> items, String id) {
        return items.stream().filter(i -> i.toString().contains("id=" + id + ",")).findFirst().orElseThrow();
    }
}
