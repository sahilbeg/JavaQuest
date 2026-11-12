package dev.javaquest.career;

import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.QuizSession;
import dev.javaquest.content.Topic;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CareerController {

    /** Seeds travel through JavaScript, whose numbers hold integers exactly only up to 2^53 - 1. */
    private static final long MAX_BROWSER_SAFE_SEED = (1L << 53) - 1;
    private static final int EXPORT_VERSION = 1;

    public record ReviewAttempt(long seed, List<QuizSession.Presented> questions) {}

    public record ReviewAnswers(long seed, Map<String, Object> answers) {}

    public record ReviewResult(QuizSession.Result result, GameRules.ReviewGrade grade) {}

    public record Export(int version, Map<String, List<Map<String, Object>>> tables) {}

    private final CareerService career;
    private final CareerReport report;
    private final CareerStore store;
    private final ContentCatalog catalog;

    public CareerController(CareerService career, CareerReport report, CareerStore store, ContentCatalog catalog) {
        this.career = career;
        this.report = report;
        this.store = store;
        this.catalog = catalog;
    }

    @GetMapping("/career")
    public CareerReport.Summary summary() {
        return report.summary();
    }

    @PostMapping("/topics/{module}/{topic}/read")
    public void read(@PathVariable String module, @PathVariable String topic) {
        career.onLessonRead(find(module, topic));
    }

    @GetMapping("/reviews/{module}/{topic}")
    public ReviewAttempt review(@PathVariable String module, @PathVariable String topic) {
        Topic found = dev.javaquest.web.TopicController.withQuiz(find(module, topic));
        long seed = ThreadLocalRandom.current().nextLong(1, MAX_BROWSER_SAFE_SEED);
        return new ReviewAttempt(seed, QuizSession.draw(found.quiz(), seed, CareerService.REVIEW_QUESTIONS));
    }

    @PostMapping("/reviews/{module}/{topic}")
    public ReviewResult gradeReview(@PathVariable String module, @PathVariable String topic, @RequestBody ReviewAnswers answers) {
        Topic found = dev.javaquest.web.TopicController.withQuiz(find(module, topic));
        var result = QuizSession.grade(found.quiz(), answers.seed(), CareerService.REVIEW_QUESTIONS, answers.answers());
        return new ReviewResult(result, career.recordReview(found, result));
    }

    @GetMapping("/progress/export")
    public Export export() {
        var tables = new LinkedHashMap<String, List<Map<String, Object>>>();
        for (String table : CareerStore.EXPORTED_TABLES) {
            tables.put(table, store.dump(table));
        }
        return new Export(EXPORT_VERSION, tables);
    }

    @PostMapping("/progress/import")
    @Transactional
    public void importProgress(@RequestBody Export export) {
        if (export.version() != EXPORT_VERSION || export.tables() == null) {
            throw new IllegalArgumentException("This file is not a JavaQuest progress export.");
        }
        for (String table : CareerStore.EXPORTED_TABLES) {
            store.replace(table, export.tables().getOrDefault(table, List.of()));
        }
    }

    private Topic find(String module, String topic) {
        String id = module + "/" + topic;
        return catalog.topic(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No topic " + id));
    }
}
