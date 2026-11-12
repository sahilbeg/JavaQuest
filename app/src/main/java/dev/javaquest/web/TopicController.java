package dev.javaquest.web;

import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.ModuleInfo;
import dev.javaquest.content.QuizSession;
import dev.javaquest.content.Topic;
import dev.javaquest.content.TopicMeta;
import dev.javaquest.lesson.LessonRenderer;
import dev.javaquest.practice.PracticeResult;
import dev.javaquest.practice.PracticeResult.Source;
import dev.javaquest.practice.PracticeService;
import dev.javaquest.practice.ProgramRun;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class TopicController {

    public record TopicSummary(String id, String moduleId, String title, String practiceKind,
                               TopicMeta.Ticket ticket, PracticeResult latest) {}

    public record Catalog(List<ModuleInfo> modules, List<TopicSummary> topics) {}

    public record TopicDetail(TopicSummary summary, int minutes, String mainClass, String lessonHtml,
                              int hintCount, String previousId, String nextId, List<String> sourceFiles) {}

    public record QuizAttempt(long seed, int passPercent, List<QuizSession.Presented> questions) {}

    public record QuizAnswers(long seed, Map<String, Object> answers) {}

    public record SourceFile(String path, String content) {}

    public record Snippet(String code) {}

    public record Problem(String message) {}

    /** Seeds travel through JavaScript, whose numbers hold integers exactly only up to 2^53 - 1. */
    static final long MAX_BROWSER_SAFE_SEED = (1L << 53) - 1;

    private final ContentCatalog catalog;
    private final PracticeService practice;
    private final LessonRenderer lessons;
    private final EventHub events;

    public TopicController(ContentCatalog catalog, PracticeService practice, LessonRenderer lessons, EventHub events) {
        this.catalog = catalog;
        this.practice = practice;
        this.lessons = lessons;
        this.events = events;
    }

    @GetMapping("/catalog")
    public Catalog catalog() {
        return new Catalog(catalog.modules(), catalog.topics().stream().map(this::summary).toList());
    }

    @GetMapping("/topics/{module}/{topic}")
    public TopicDetail topic(@PathVariable String module, @PathVariable String topic) throws IOException {
        Topic found = find(module, topic);
        List<Topic> all = catalog.topics();
        int index = all.indexOf(found);
        return new TopicDetail(summary(found), found.meta().minutes(), found.meta().practice().mainClass(),
                lessons.render(Files.readString(found.lessonFile())), found.meta().hints().size(),
                index > 0 ? all.get(index - 1).id() : null,
                index < all.size() - 1 ? all.get(index + 1).id() : null,
                practice.sourceFiles(found));
    }

    @GetMapping("/topics/{module}/{topic}/hints/{number}")
    public Map<String, String> hint(@PathVariable String module, @PathVariable String topic, @PathVariable int number) {
        List<String> hints = find(module, topic).meta().hints();
        if (number < 1 || number > hints.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hint " + number);
        }
        return Map.of("hint", hints.get(number - 1));
    }

    @GetMapping("/topics/{module}/{topic}/quiz")
    public QuizAttempt quiz(@PathVariable String module, @PathVariable String topic, @RequestParam(required = false) Long seed) {
        Topic found = find(module, topic);
        long chosen = seed != null ? seed : ThreadLocalRandom.current().nextLong(1, MAX_BROWSER_SAFE_SEED);
        return new QuizAttempt(chosen, found.quiz().passPercent(), QuizSession.draw(found.quiz(), chosen));
    }

    @PostMapping("/topics/{module}/{topic}/quiz")
    public QuizSession.Result gradeQuiz(@PathVariable String module, @PathVariable String topic, @RequestBody QuizAnswers answers) {
        return QuizSession.grade(find(module, topic).quiz(), answers.seed(), answers.answers());
    }

    @GetMapping("/topics/{module}/{topic}/source")
    public SourceFile readSource(@PathVariable String module, @PathVariable String topic, @RequestParam String path)
            throws IOException {
        return new SourceFile(path, practice.readSource(find(module, topic), path));
    }

    @PutMapping("/topics/{module}/{topic}/source")
    public void writeSource(@PathVariable String module, @PathVariable String topic, @RequestBody SourceFile file)
            throws IOException {
        practice.writeSource(find(module, topic), file.path(), file.content());
    }

    @PostMapping("/topics/{module}/{topic}/submit")
    public PracticeResult submit(@PathVariable String module, @PathVariable String topic)
            throws IOException, InterruptedException {
        return practice.submit(find(module, topic), Source.BROWSER);
    }

    @PostMapping("/topics/{module}/{topic}/run")
    public ProgramRun run(@PathVariable String module, @PathVariable String topic) throws IOException, InterruptedException {
        Topic found = find(module, topic);
        String mainClass = found.meta().practice().mainClass();
        if (mainClass == null) {
            throw new IllegalArgumentException("This ticket has no program to run. Use Submit to run the checks.");
        }
        return practice.runMain(found, mainClass);
    }

    @PostMapping("/snippets/run")
    public ProgramRun runSnippet(@RequestBody Snippet snippet) throws IOException, InterruptedException {
        return practice.runSnippet(snippet.code());
    }

    @GetMapping(path = "/events", produces = "text/event-stream")
    public SseEmitter events() {
        return events.subscribe();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Problem badRequest(IllegalArgumentException e) {
        return new Problem(e.getMessage());
    }

    private TopicSummary summary(Topic t) {
        return new TopicSummary(t.id(), t.meta().moduleId(), t.meta().title(), t.meta().practice().kind().name(),
                t.meta().ticket(), practice.latest(t.id()).orElse(null));
    }

    private Topic find(String module, String topic) {
        String id = module + "/" + topic;
        return catalog.topic(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No topic " + id));
    }
}
