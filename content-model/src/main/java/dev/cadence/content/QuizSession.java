package dev.cadence.content;

import dev.cadence.content.Quiz.FillBlank;
import dev.cadence.content.Quiz.Mcq;
import dev.cadence.content.Quiz.McqVariant;
import dev.cadence.content.Quiz.Order;
import dev.cadence.content.Quiz.PredictOutput;
import dev.cadence.content.Quiz.Question;
import dev.cadence.content.Quiz.SpotTheBug;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.stream.IntStream;

/**
 * Draws a quiz attempt from a seed and grades it. Everything shown to the learner is derived from the
 * seed, so grading recomputes the same variants and shuffles without storing the attempt.
 */
public final class QuizSession {

    /** A question as the page sees it. {@code goal} is set for fill-blank questions only. */
    public record Presented(String id, String type, String prompt, String code, List<String> choices, List<String> items,
                            String goal) {
        public Presented(String id, String type, String prompt, String code, List<String> choices, List<String> items) {
            this(id, type, prompt, code, choices, items, null);
        }
    }

    public record Graded(String id, String type, boolean correct, String concept, String expected, String explain, String reteach) {}

    public record Result(int scorePercent, boolean passed, List<Graded> questions, List<String> weakConcepts) {}

    private QuizSession() {}

    public static List<Presented> draw(Quiz quiz, long seed) {
        return pick(quiz, seed).stream().map(q -> present(q, seed)).toList();
    }

    public static Result grade(Quiz quiz, long seed, Map<String, Object> answers) {
        var graded = pick(quiz, seed).stream().map(q -> gradeOne(q, seed, answers.get(q.id()))).toList();
        long correct = graded.stream().filter(Graded::correct).count();
        int score = (int) (correct * 100 / graded.size());
        var weak = graded.stream().filter(g -> !g.correct()).map(Graded::concept).distinct().toList();
        return new Result(score, score >= quiz.passPercent(), graded, weak);
    }

    static List<Question> pick(Quiz quiz, long seed) {
        var shuffled = new ArrayList<>(quiz.questions());
        Collections.shuffle(shuffled, new SplittableRandom(seed));
        var chosen = new LinkedHashSet<Question>();
        var concepts = new HashSet<String>();
        for (Question q : shuffled) {
            if (chosen.size() < quiz.drawCount() && concepts.add(q.concept())) {
                chosen.add(q);
            }
        }
        for (Question q : shuffled) {
            if (chosen.size() < quiz.drawCount()) {
                chosen.add(q);
            }
        }
        return List.copyOf(chosen);
    }

    private static Presented present(Question question, long seed) {
        SplittableRandom random = randomFor(question, seed);
        return switch (question) {
            case Mcq mcq -> {
                McqVariant variant = variant(mcq, random);
                List<Integer> order = permutation(variant.choices().size(), random, false);
                yield new Presented(mcq.id(), "mcq", variant.prompt(), null,
                        order.stream().map(variant.choices()::get).toList(), null);
            }
            case FillBlank fill -> new Presented(fill.id(), "fill-blank", fill.prompt(), null, null, null, fill.goal());
            case PredictOutput predict -> new Presented(predict.id(), "predict-output",
                    predict.prompt() == null ? "What does this program print?" : predict.prompt(), predict.code(), null, null);
            case Order order -> new Presented(order.id(), "order", order.prompt(), null, null,
                    permutation(order.items().size(), random, true).stream().map(order.items()::get).toList());
            case SpotTheBug bug -> new Presented(bug.id(), "spot-the-bug",
                    bug.prompt() == null ? "Which line has the bug?" : bug.prompt(), bug.code(), null, null);
        };
    }

    private static Graded gradeOne(Question question, long seed, Object answer) {
        SplittableRandom random = randomFor(question, seed);
        return switch (question) {
            case Mcq mcq -> {
                McqVariant variant = variant(mcq, random);
                List<Integer> order = permutation(variant.choices().size(), random, false);
                boolean correct = answer instanceof Integer i && i >= 0 && i < order.size() && order.get(i) == variant.answer();
                yield graded(mcq, correct, variant.choices().get(variant.answer()));
            }
            case FillBlank fill -> graded(fill,
                    answer instanceof String s && fill.answers().contains(s.strip()), fill.answers().getFirst());
            case PredictOutput predict -> graded(predict,
                    answer instanceof String s && normalizeOutput(s).equals(normalizeOutput(predict.answer())), predict.answer());
            case Order order -> {
                List<Integer> shown = permutation(order.items().size(), random, true);
                boolean correct = answer instanceof List<?> list && list.size() == shown.size()
                        && IntStream.range(0, list.size()).allMatch(i ->
                                list.get(i) instanceof Integer p && p >= 0 && p < shown.size() && shown.get(p) == i);
                yield graded(order, correct, String.join("\n", order.items()));
            }
            case SpotTheBug bug -> graded(bug, answer instanceof Integer line && line == bug.bugLine(), "line " + bug.bugLine());
        };
    }

    private static Graded graded(Question q, boolean correct, String expected) {
        return new Graded(q.id(), typeName(q), correct, q.concept(), expected, q.explain(), q.reteach());
    }

    static String typeName(Question question) {
        return switch (question) {
            case Mcq m -> "mcq";
            case FillBlank f -> "fill-blank";
            case PredictOutput p -> "predict-output";
            case Order o -> "order";
            case SpotTheBug b -> "spot-the-bug";
        };
    }

    /** Draws a smaller attempt from the same pool, for spaced-repetition reviews. */
    public static List<Presented> draw(Quiz quiz, long seed, int count) {
        return draw(sized(quiz, count), seed);
    }

    public static Result grade(Quiz quiz, long seed, int count, Map<String, Object> answers) {
        return grade(sized(quiz, count), seed, answers);
    }

    private static Quiz sized(Quiz quiz, int count) {
        return new Quiz(quiz.topic(), Math.min(count, quiz.questions().size()), quiz.passPercent(), quiz.questions());
    }

    public static String normalizeOutput(String output) {
        return output.replace("\r\n", "\n").lines().map(String::stripTrailing).reduce((a, b) -> a + "\n" + b).orElse("").strip();
    }

    private static SplittableRandom randomFor(Question question, long seed) {
        return new SplittableRandom(seed * 31 + question.id().hashCode());
    }

    private static McqVariant variant(Mcq mcq, SplittableRandom random) {
        return mcq.variants().get(random.nextInt(mcq.variants().size()));
    }

    private static List<Integer> permutation(int size, SplittableRandom random, boolean avoidIdentity) {
        var order = new ArrayList<>(IntStream.range(0, size).boxed().toList());
        Collections.shuffle(order, random);
        if (avoidIdentity && size > 1 && IntStream.range(0, size).allMatch(i -> order.get(i) == i)) {
            Collections.rotate(order, 1);
        }
        return order;
    }
}
