package dev.javaquest.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TopicMeta(
        String id,
        String title,
        int order,
        int minutes,
        List<String> prerequisites,
        List<String> concepts,
        Practice practice,
        Xp xp,
        Ticket ticket,
        List<String> hints,
        boolean review) {

    public enum PracticeKind {
        @JsonProperty("code") CODE,
        @JsonProperty("quiz") QUIZ
    }

    public enum RunnerKind {
        @JsonProperty("plain") PLAIN,
        @JsonProperty("mockito") MOCKITO,
        @JsonProperty("spring") SPRING
    }

    public record Practice(PracticeKind kind, RunnerKind runner, String mainClass, Integer timeoutSeconds) {
        public Practice {
            require(kind != null, "practice.kind is required");
            runner = runner == null ? RunnerKind.PLAIN : runner;
        }
    }

    public record Xp(int quiz, int code) {}

    public enum Teammate {
        @JsonProperty("maya") MAYA,
        @JsonProperty("theo") THEO,
        @JsonProperty("priya") PRIYA,
        @JsonProperty("sam") SAM
    }

    public record Ticket(String key, Teammate from, String title, String body) {
        public Ticket {
            require(key != null && key.matches("SPK-\\d{4}|INC-\\d{2}|FEAT-\\d{2}|CAPSTONE"),
                    "ticket.key must look like SPK-0105, INC-01, FEAT-01 or CAPSTONE, got: " + key);
            require(from != null, "ticket.from is required");
            require(title != null && !title.isBlank(), "ticket.title is required");
            require(body != null && !body.isBlank(), "ticket.body is required");
            body = body.strip();
        }
    }

    public TopicMeta {
        require(id != null && id.matches("module-\\d{2}-[a-z0-9-]+/(topic-\\d{2}-[a-z0-9-]+|boss|mini-project)"),
                "id must look like module-NN-name/topic-NN-name, got: " + id);
        require(title != null && !title.isBlank(), "title is required");
        require(minutes >= 1 && minutes <= 15, "minutes must be between 1 and 15, got: " + minutes);
        require(practice != null, "practice is required");
        require(ticket != null, "ticket is required");
        require(hints != null && hints.size() == 3, "exactly 3 hints are required");
        prerequisites = prerequisites == null ? List.of() : List.copyOf(prerequisites);
        concepts = concepts == null ? List.of() : List.copyOf(concepts);
        hints = List.copyOf(hints);
        xp = xp == null ? new Xp(50, 100) : xp;
    }

    public boolean isBoss() {
        return id.endsWith("/boss");
    }

    public boolean isFeature() {
        return id.endsWith("/mini-project");
    }

    public String moduleId() {
        return id.substring(0, id.indexOf('/'));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
