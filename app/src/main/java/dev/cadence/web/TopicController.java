package dev.cadence.web;

import dev.cadence.content.ContentCatalog;
import dev.cadence.content.ModuleInfo;
import dev.cadence.content.QuizSession;
import dev.cadence.content.Topic;
import dev.cadence.content.TopicMeta;
import dev.cadence.lesson.LessonRenderer;
import dev.cadence.lesson.QuickReference;
import dev.cadence.practice.PracticeResult;
import dev.cadence.practice.PracticeResult.Source;
import dev.cadence.practice.CodeGuide;
import dev.cadence.practice.PracticeService;
import dev.cadence.practice.ProgramRun;
import dev.cadence.progress.Gate;
import dev.cadence.progress.LessonPosition;
import dev.cadence.progress.LockedException;
import dev.cadence.progress.ProgressService;
import dev.cadence.progress.ProgressService.TopicProgress;
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
                               TopicMeta.Ticket ticket, PracticeResult latest, TopicProgress progress, boolean locked) {}

    /**
     * frontierId is the first ticket the learner has not mastered: the one to work on now. today says whether the
     * daily limit is on and reached; when it is, the frontier is locked until tomorrow.
     */
    public record Catalog(List<ModuleInfo> modules, List<TopicSummary> topics, String frontierId, boolean authorMode,
                          Gate.Today today, CourseRef course) {}

    /** The active course, so pages can name it. {@code chosen} is false until the learner picks one. */
    public record CourseRef(String id, String title, boolean chosen) {}

    public record TopicDetail(TopicSummary summary, int minutes, String mainClass, String lessonHtml,
                              int hintCount, String previousId, String nextId, List<String> sourceFiles,
                              boolean hasQuiz, String kind, CodeGuide codeGuide) {}

    public record QuizAttempt(long seed, int passPercent, List<QuizSession.Presented> questions) {}

    public record QuizAnswers(long seed, Map<String, Object> answers) {}

    public record QuizGraded(QuizSession.Result result, TopicProgress progress) {}

    public record Hints(List<String> opened, TopicProgress progress) {}

    public record OpenedHint(String hint, TopicProgress progress) {}

    public record Solution(List<ProgressService.SourceFile> files, TopicProgress progress) {}

    public record SourceFile(String path, String content) {}

    public record Snippet(String code) {}

    /** What the learner typed in the Program input box. It reaches the program as standard input. */
    public record RunRequest(String input) {}

    /** Enough for any practice program; keeps a pasted novel out of the runner. */
    static final int MAX_INPUT_CHARS = 10_000;

    /** The screen to come back to: an unfinished ticket's card, with what the Continue tab needs to show. */
    public record Resume(String topicId, String key, String ticketTitle, int step, int total, String label) {}


    /** Seeds travel through JavaScript, whose numbers hold integers exactly only up to 2^53 - 1. */
    static final long MAX_BROWSER_SAFE_SEED = (1L << 53) - 1;

    private final ContentCatalog catalog;
    private final PracticeService practice;
    private final LessonRenderer lessons;
    private final EventHub events;
    private final ProgressService progress;
    private final Gate gate;
    private final QuickReference quickReference;
    private final dev.cadence.course.Courses courses;

    public TopicController(ContentCatalog catalog, PracticeService practice, LessonRenderer lessons, EventHub events,
                           ProgressService progress, Gate gate, QuickReference quickReference, dev.cadence.course.Courses courses) {
        this.courses = courses;
        this.catalog = catalog;
        this.practice = practice;
        this.lessons = lessons;
        this.events = events;
        this.progress = progress;
        this.gate = gate;
        this.quickReference = quickReference;
    }

    @GetMapping("/catalog")
    public Catalog catalog() {
        Gate.Today today = gate.today();
        Map<String, Gate.Access> access = gate.accessAll(today);
        String frontier = access.values().stream().map(Gate.Access::frontierId).findFirst().orElse(null);
        ContentCatalog course = courses.catalog();
        return new Catalog(course.modules(),
                course.topics().stream().map(t -> summary(t, access.get(t.id()).locked())).toList(),
                frontier, gate.authorMode(), today,
                new CourseRef(courses.activeId(), courses.active().title(), courses.chosen().isPresent()));
    }

    @GetMapping("/topics/{module}/{topic}")
    public TopicDetail topic(@PathVariable String module, @PathVariable String topic) throws IOException {
        Topic found = open(module, topic);
        List<Topic> all = courses.catalog().topics();
        int index = all.indexOf(found);
        return new TopicDetail(summary(found, false), found.meta().minutes(), found.meta().practice().mainClass(),
                lessons.render(Files.readString(found.lessonFile())), found.meta().hints().size(),
                index > 0 ? all.get(index - 1).id() : null,
                index < all.size() - 1 ? all.get(index + 1).id() : null,
                practice.sourceFiles(found), found.hasQuiz(),
                found.meta().isBoss() ? "incident" : found.meta().isFeature() ? "feature" : "ticket",
                practice.codeGuide(found).orElse(null));
    }

    /** Where the learner is inside this ticket, or nothing when they have not opened it in the player yet. */
    @GetMapping("/topics/{module}/{topic}/position")
    public LessonPosition position(@PathVariable String module, @PathVariable String topic) {
        return progress.position(open(module, topic)).orElse(null);
    }

    @PutMapping("/topics/{module}/{topic}/position")
    public void savePosition(@PathVariable String module, @PathVariable String topic, @RequestBody LessonPosition position) {
        progress.savePosition(open(module, topic), position);
    }

    /**
     * The most recent screen in a ticket that is open and not finished yet: where Continue takes the learner after a
     * rebuild, a hard reload or a visit to another page. Nothing when every saved place is finished or locked.
     */
    @GetMapping("/resume")
    public Resume resume() {
        Map<String, Gate.Access> access = gate.accessAll();
        for (var saved : progress.positions()) {
            var topic = catalog.topic(saved.topicId());
            Gate.Access open = access.get(saved.topicId());
            if (topic.isEmpty() || open == null || open.locked() || progress.progress(topic.get()).mastered()) {
                continue;
            }
            var position = saved.position();
            return new Resume(saved.topicId(), topic.get().meta().ticket().key(), topic.get().meta().ticket().title(),
                    position.step(), position.total(), position.label());
        }
        return null;
    }

    /** Quick reference notes, taken from the lesson. They open once the ticket is finished (any time in author mode). */
    @GetMapping("/topics/{module}/{topic}/notes")
    public QuickReference.Notes notes(@PathVariable String module, @PathVariable String topic) throws IOException {
        Topic found = open(module, topic);
        if (!gate.authorMode() && !progress.progress(found).mastered()) {
            throw new LockedException("Quick reference notes open once you finish this ticket.");
        }
        return quickReference.notes(Files.readString(found.lessonFile()));
    }

    @GetMapping("/topics/{module}/{topic}/hints")
    public Hints hints(@PathVariable String module, @PathVariable String topic) {
        Topic found = open(module, topic);
        return new Hints(progress.openedHints(found), progress.progress(found));
    }

    @PostMapping("/topics/{module}/{topic}/hints/next")
    public OpenedHint nextHint(@PathVariable String module, @PathVariable String topic) {
        Topic found = open(module, topic);
        String hint = progress.openNextHint(found);
        return new OpenedHint(hint, progress.progress(found));
    }

    @GetMapping("/topics/{module}/{topic}/progress")
    public TopicSummary progress(@PathVariable String module, @PathVariable String topic) {
        Topic found = find(module, topic);
        return summary(found, gate.access(found).locked());
    }

    @PostMapping("/topics/{module}/{topic}/solution")
    public Solution solution(@PathVariable String module, @PathVariable String topic) throws IOException {
        Topic found = open(module, topic);
        var files = progress.openSolution(found);
        return new Solution(files, progress.progress(found));
    }

    @PostMapping("/topics/{module}/{topic}/reset")
    public void reset(@PathVariable String module, @PathVariable String topic) throws IOException {
        practice.resetToSkeleton(open(module, topic));
    }

    @GetMapping("/topics/{module}/{topic}/quiz")
    public QuizAttempt quiz(@PathVariable String module, @PathVariable String topic, @RequestParam(required = false) Long seed) {
        Topic found = withQuiz(open(module, topic));
        long chosen = seed != null ? seed : ThreadLocalRandom.current().nextLong(1, MAX_BROWSER_SAFE_SEED);
        return new QuizAttempt(chosen, found.quiz().passPercent(), QuizSession.draw(found.quiz(), chosen));
    }

    @PostMapping("/topics/{module}/{topic}/quiz")
    public QuizGraded gradeQuiz(@PathVariable String module, @PathVariable String topic, @RequestBody QuizAnswers answers) {
        Topic found = withQuiz(open(module, topic));
        var result = QuizSession.grade(found.quiz(), answers.seed(), answers.answers());
        return new QuizGraded(result, progress.recordQuiz(found, answers.seed(), result));
    }

    @GetMapping("/topics/{module}/{topic}/source")
    public SourceFile readSource(@PathVariable String module, @PathVariable String topic, @RequestParam String path)
            throws IOException {
        return new SourceFile(path, practice.readSource(open(module, topic), path));
    }

    @PutMapping("/topics/{module}/{topic}/source")
    public void writeSource(@PathVariable String module, @PathVariable String topic, @RequestBody SourceFile file)
            throws IOException {
        practice.writeSource(open(module, topic), file.path(), file.content());
    }

    @PostMapping("/topics/{module}/{topic}/submit")
    public PracticeResult submit(@PathVariable String module, @PathVariable String topic)
            throws IOException, InterruptedException {
        return practice.submit(open(module, topic), Source.BROWSER);
    }

    @PostMapping("/topics/{module}/{topic}/run")
    public ProgramRun run(@PathVariable String module, @PathVariable String topic,
            @RequestBody(required = false) RunRequest request) throws IOException, InterruptedException {
        Topic found = open(module, topic);
        String mainClass = found.meta().practice().mainClass();
        if (mainClass == null) {
            throw new IllegalArgumentException("This ticket has no program to run. Use Submit to run the checks.");
        }
        String input = request == null || request.input() == null ? "" : request.input();
        if (input.length() > MAX_INPUT_CHARS) {
            throw new IllegalArgumentException("The program input is too long. Keep it under " + MAX_INPUT_CHARS + " characters.");
        }
        return practice.runMain(found, mainClass, input);
    }

    @PostMapping("/snippets/run")
    public ProgramRun runSnippet(@RequestBody Snippet snippet) throws IOException, InterruptedException {
        return practice.runSnippet(snippet.code());
    }

    @GetMapping(path = "/events", produces = "text/event-stream")
    public SseEmitter events() {
        return events.subscribe();
    }

    private TopicSummary summary(Topic t, boolean locked) {
        return new TopicSummary(t.id(), t.meta().moduleId(), t.meta().title(), t.meta().practice().kind().name(),
                t.meta().ticket(), practice.latest(t.id()).orElse(null), progress.progress(t), locked);
    }

    public static Topic withQuiz(Topic topic) {
        if (!topic.hasQuiz()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, topic.id() + " has no onboarding check");
        }
        return topic;
    }

    /** A ticket the learner has reached. A locked one answers 403 with what to finish first. */
    private Topic open(String module, String topic) {
        return gate.requireOpen(find(module, topic));
    }

    private Topic find(String module, String topic) {
        String id = module + "/" + topic;
        return catalog.topic(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No topic " + id));
    }
}
