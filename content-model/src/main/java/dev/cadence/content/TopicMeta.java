package dev.cadence.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
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

        /** A Spring topic starts a whole application context, so it gets at least this long, unless timeoutSeconds says otherwise. */
        public static final Duration SPRING_TIMEOUT = Duration.ofSeconds(60);
        /** A Spring topic gets at least this much heap. */
        public static final int SPRING_HEAP_MEGABYTES = 512;

        public Practice {
            require(kind != null, "practice.kind is required");
            require(timeoutSeconds == null || timeoutSeconds > 0, "practice.timeoutSeconds must be positive, got: " + timeoutSeconds);
            runner = runner == null ? RunnerKind.PLAIN : runner;
        }

        /** How long a run may take: timeoutSeconds when set, otherwise the default, raised to SPRING_TIMEOUT for Spring. */
        public Duration timeout(Duration defaultTimeout) {
            if (timeoutSeconds != null) {
                return Duration.ofSeconds(timeoutSeconds);
            }
            if (runner == RunnerKind.SPRING && defaultTimeout.compareTo(SPRING_TIMEOUT) < 0) {
                return SPRING_TIMEOUT;
            }
            return defaultTimeout;
        }

        /** The heap for a run: the default, raised to SPRING_HEAP_MEGABYTES for Spring. */
        public int heapMegabytes(int defaultHeapMegabytes) {
            return runner == RunnerKind.SPRING ? Math.max(defaultHeapMegabytes, SPRING_HEAP_MEGABYTES) : defaultHeapMegabytes;
        }
    }

    public record Xp(int quiz, int code) {}

    public enum Teammate {
        @JsonProperty("maya") MAYA,
        @JsonProperty("theo") THEO,
        @JsonProperty("priya") PRIYA,
        @JsonProperty("sam") SAM,
        /** Sana, the designer, who sends tickets in the web courses. */
        @JsonProperty("sana") SANA
    }

    public record Ticket(String key, Teammate from, String title, String body) {
        public Ticket {
            require(key != null && key.matches("(SPK|DHG|WEB)-\\d{4}|INC-\\d{2}|FEAT-\\d{2}|CAPSTONE"),
                    "ticket.key must look like SPK-0105 (Spoke), DHG-0201 (Dhaaga, Java), WEB-0304 (web), INC-01, FEAT-01 or CAPSTONE, got: " + key);
            require(from != null, "ticket.from is required");
            require(title != null && !title.isBlank(), "ticket.title is required");
            require(body != null && !body.isBlank(), "ticket.body is required");
            body = body.strip();
        }
    }

    public TopicMeta {
        require(id != null && id.matches(ModuleInfo.ID_PATTERN + "/(topic-\\d{2}-[a-z0-9-]+|boss|mini-project)"),
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
