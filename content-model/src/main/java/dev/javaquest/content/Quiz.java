package dev.javaquest.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashSet;
import java.util.List;

public record Quiz(String topic, int drawCount, int passPercent, List<Question> questions) {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Mcq.class, name = "mcq"),
        @JsonSubTypes.Type(value = FillBlank.class, name = "fill-blank"),
        @JsonSubTypes.Type(value = PredictOutput.class, name = "predict-output"),
        @JsonSubTypes.Type(value = Order.class, name = "order"),
        @JsonSubTypes.Type(value = SpotTheBug.class, name = "spot-the-bug")
    })
    public sealed interface Question permits Mcq, FillBlank, PredictOutput, Order, SpotTheBug {
        String id();
        String concept();
        String explain();
        String reteach();
    }

    public record McqVariant(String prompt, List<String> choices, int answer) {
        public McqVariant {
            require(prompt != null && !prompt.isBlank(), "mcq prompt is required");
            require(choices != null && choices.size() >= 2, "mcq needs at least 2 choices");
            require(answer >= 0 && answer < choices.size(), "mcq answer must index a choice, got: " + answer);
            choices = List.copyOf(choices);
        }
    }

    public record Mcq(String id, String concept, String prompt, List<String> choices, Integer answer,
                      List<McqVariant> variants, String explain, String reteach) implements Question {
        public Mcq {
            boolean single = prompt != null || choices != null || answer != null;
            require(single != (variants != null && !variants.isEmpty()),
                    "mcq " + id + " needs either prompt, choices and answer, or a variants list");
            if (single) {
                variants = List.of(new McqVariant(prompt, choices, answer == null ? -1 : answer));
            } else {
                variants = List.copyOf(variants);
            }
            requireCommon(id, concept, explain);
        }
    }

    public record FillBlank(String id, String concept, String prompt, List<String> answers,
                            String explain, String reteach) implements Question {
        public FillBlank {
            requireCommon(id, concept, explain);
            require(prompt != null && prompt.contains("___"), "fill-blank " + id + " prompt needs a ___ blank");
            require(answers != null && !answers.isEmpty(), "fill-blank " + id + " needs answers");
            answers = List.copyOf(answers);
        }
    }

    public record PredictOutput(String id, String concept, String prompt, String code, String answer,
                                String explain, String reteach) implements Question {
        public PredictOutput {
            requireCommon(id, concept, explain);
            require(code != null && !code.isBlank(), "predict-output " + id + " needs code");
            require(answer != null, "predict-output " + id + " needs an answer");
        }
    }

    public record Order(String id, String concept, String prompt, List<String> items,
                        String explain, String reteach) implements Question {
        public Order {
            requireCommon(id, concept, explain);
            require(prompt != null && !prompt.isBlank(), "order " + id + " needs a prompt");
            require(items != null && items.size() >= 2, "order " + id + " needs at least 2 items");
            require(new HashSet<>(items).size() == items.size(), "order " + id + " items must all differ");
            items = List.copyOf(items);
        }
    }

    public record SpotTheBug(String id, String concept, String prompt, String code, int bugLine,
                             String explain, String reteach) implements Question {
        public SpotTheBug {
            requireCommon(id, concept, explain);
            require(code != null && !code.isBlank(), "spot-the-bug " + id + " needs code");
            long lines = code.lines().count();
            require(bugLine >= 1 && bugLine <= lines, "spot-the-bug " + id + " bugLine must be 1 to " + lines);
        }
    }

    public Quiz {
        require(questions != null && !questions.isEmpty(), "a quiz needs questions");
        require(drawCount >= 1 && drawCount <= questions.size(),
                "drawCount must be between 1 and the number of questions (" + questions.size() + ")");
        require(passPercent >= 1 && passPercent <= 100, "passPercent must be 1 to 100");
        require(questions.stream().map(Question::id).distinct().count() == questions.size(), "question ids must be unique");
        questions = List.copyOf(questions);
    }

    private static void requireCommon(String id, String concept, String explain) {
        require(id != null && !id.isBlank(), "every question needs an id");
        require(concept != null && !concept.isBlank(), "question " + id + " needs a concept");
        require(explain != null && !explain.isBlank(), "question " + id + " needs an explain");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
