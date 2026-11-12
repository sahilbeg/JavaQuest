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
import dev.javaquest.progress.ProgressService;
import dev.javaquest.progress.ProgressService.TopicProgress;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class TopicController {

    public record TopicSummary(String id, String moduleId, String title, String practiceKind,
                               TopicMeta.Ticket ticket, PracticeResult latest, TopicProgress progress) {}

    public record Catalog(List<ModuleInfo> modules, List<TopicSummary> topics) {}

    public record TopicDetail(TopicSummary summary, int minutes, String mainClass, String lessonHtml,
                              int hintCount, String previousId, String nextId, List<String> sourceFiles,
                              boolean hasQuiz, String kind) {}

    public record QuizAttempt(long seed, int passPercent, List<QuizSession.Presented> questions) {}

    public record QuizAnswers(long seed, Map<String, Object> answers) {}

    public record QuizGraded(QuizSession.Result result, TopicProgress progress) {}

    public record Hints(List<String> opened, TopicProgress progress) {}

    public record OpenedHint(String hint, TopicProgress progress) {}

    public record Solution(List<ProgressService.SourceFile> files, TopicProgress progress) {}

    public record SourceFile(String path, String content) {}

    public record Snippet(String code) {}


    /** Seeds travel through JavaScript, whose numbers hold integers exactly only up to 2^53 - 1. */
    static final long MAX_BROWSER_SAFE_SEED = (1L << 53) - 1;

    private final ContentCatalog catalog;
    private final PracticeService practice;
    private final LessonRenderer lessons;
    private final EventHub events;
    private final ProgressService progress;

    public TopicController(ContentCatalog catalog, PracticeService practice, LessonRenderer lessons, EventHub events,
                           ProgressService progress) {
        this.catalog = catalog;
        this.practice = practice;
        this.lessons = lessons;
        this.events = events;
        this.progress = progress;
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
                practice.sourceFiles(found), found.hasQuiz(),
                found.meta().isBoss() ? "incident" : found.meta().isFeature() ? "feature" : "ticket");
    }

    @GetMapping("/topics/{module}/{topic}/hints")
    public Hints hints(@PathVariable String module, @PathVariable String topic) {
        Topic found = find(module, topic);
        return new Hints(progress.openedHints(found), progress.progress(found));
    }

    @PostMapping("/topics/{module}/{topic}/hints/next")
    public OpenedHint nextHint(@PathVariable String module, @PathVariable String topic) {
        Topic found = find(module, topic);
        String hint = progress.openNextHint(found);
        return new OpenedHint(hint, progress.progress(found));
    }

    @GetMapping("/topics/{module}/{topic}/progress")
    public TopicSummary progress(@PathVariable String module, @PathVariable String topic) {
        return summary(find(module, topic));
    }

    @PostMapping("/topics/{module}/{topic}/solution")
    public Solution solution(@PathVariable String module, @PathVariable String topic) throws IOException {
        Topic found = find(module, topic);
        var files = progress.openSolution(found);
        return new Solution(files, progress.progress(found));
    }

    @PostMapping("/topics/{module}/{topic}/reset")
    public void reset(@PathVariable String module, @PathVariable String topic) throws IOException {
        practice.resetToSkeleton(find(module, topic));
    }

    @GetMapping("/topics/{module}/{topic}/quiz")
    public QuizAttempt quiz(@PathVariable String module, @PathVariable String topic, @RequestParam(required = false) Long seed) {
        Topic found = withQuiz(find(module, topic));
        long chosen = seed != null ? seed : ThreadLocalRandom.current().nextLong(1, MAX_BROWSER_SAFE_SEED);
        return new QuizAttempt(chosen, found.quiz().passPercent(), QuizSession.draw(found.quiz(), chosen));
    }

    @PostMapping("/topics/{module}/{topic}/quiz")
    public QuizGraded gradeQuiz(@PathVariable String module, @PathVariable String topic, @RequestBody QuizAnswers answers) {
        Topic found = withQuiz(find(module, topic));
        var result = QuizSession.grade(found.quiz(), answers.seed(), answers.answers());
        return new QuizGraded(result, progress.recordQuiz(found, answers.seed(), result));
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

    private TopicSummary summary(Topic t) {
        return new TopicSummary(t.id(), t.meta().moduleId(), t.meta().title(), t.meta().practice().kind().name(),
                t.meta().ticket(), practice.latest(t.id()).orElse(null), progress.progress(t));
    }

    public static Topic withQuiz(Topic topic) {
        if (!topic.hasQuiz()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, topic.id() + " has no onboarding check");
        }
        return topic;
    }

    private Topic find(String module, String topic) {
        String id = module + "/" + topic;
        return catalog.topic(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No topic " + id));
    }
}
