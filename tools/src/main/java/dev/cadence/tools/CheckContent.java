package dev.cadence.tools;

import dev.cadence.content.Animation;
import dev.cadence.content.ContentCatalog;
import dev.cadence.content.Quiz;
import dev.cadence.content.QuizSession;
import dev.cadence.content.Topic;
import dev.cadence.runner.PracticeRunner;
import dev.cadence.runner.ProgramResult;
import dev.cadence.runner.RunLimits;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Checks every topic in content/: the files load, prerequisites name real curriculum units, every Java
 * block in a lesson compiles, runnable snippets finish, and predict-the-output answers match real output.
 * Exits with status 1 and a list of problems when anything is wrong.
 */
public final class CheckContent {

    private static final RunLimits LIMITS = new RunLimits(Duration.ofSeconds(10), 256, 65_536);

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        var runner = new PracticeRunner(PracticeRunner.currentJavaExecutable(), RunnerClasspath.read(root), LIMITS);
        var library = dev.cadence.content.CourseLibrary.load(root);
        var catalog = library.library();
        // Web topics run their code with Node; the tools are installed in the web pool's practice folder.
        var webPool = library.pools().stream().filter(p -> p.id().equals("web")).findFirst();
        Path webContent = webPool.map(p -> root.resolve(p.content()).toAbsolutePath().normalize()).orElse(null);
        var node = new dev.cadence.runner.NodeRunner(dev.cadence.runner.NodeRunner.nodeOnPath(),
                webPool.map(p -> root.resolve(p.practice())).orElse(root.resolve("tracks/web/practice")), LIMITS);
        Set<String> curriculum = curriculumIds(root.resolve("build_state.json"));
        var problems = new ArrayList<String>();
        // -Djq.only=module-01-basics/topic-03 checks only the topics whose id starts with that text.
        String only = System.getProperty("jq.only", "");

        for (Topic topic : catalog.topics()) {
            if (!topic.id().startsWith(only)) {
                continue;
            }
            for (String prerequisite : topic.meta().prerequisites()) {
                if (!curriculum.contains(prerequisite)) {
                    problems.add(topic.id() + ": prerequisite " + prerequisite + " is not in the curriculum");
                }
            }
            String lesson = Files.readString(topic.lessonFile());
            for (LessonCode.Block block : LessonCode.compilableBlocks(lesson)) {
                String problem = checkBlock(runner, block);
                if (problem != null) {
                    problems.add(topic.id() + " lesson.md line " + block.line() + ": " + problem);
                }
            }
            boolean web = webContent != null && topic.contentDir().toAbsolutePath().normalize().startsWith(webContent);
            for (LessonCode.Block block : LessonCode.javaScriptBlocks(lesson)) {
                String problem = node.ready() ? checkJavaScript(node, block)
                        : "the web tools aren't installed: run npm ci in the web practice folder";
                if (problem != null) {
                    problems.add(topic.id() + " lesson.md line " + block.line() + ": " + problem);
                }
            }
            for (LessonCode.Block block : LessonCode.animationBlocks(lesson)) {
                String problem = checkAnimation(runner, node, block);
                if (problem != null) {
                    problems.add(topic.id() + " lesson.md line " + block.line() + ": " + problem);
                }
            }
            for (Quiz.Question question : topic.hasQuiz() ? topic.quiz().questions() : List.<Quiz.Question>of()) {
                if (question instanceof Quiz.PredictOutput predict) {
                    boolean ts = topic.id().startsWith("ts-") || topic.id().startsWith("tsreact-");
                    String problem = web ? checkJavaScriptPrediction(node, predict, ts) : checkPrediction(runner, predict);
                    if (problem != null) {
                        problems.add(topic.id() + " quiz " + predict.id() + ": " + problem);
                    }
                }
            }
            System.out.println("checked " + topic.id());
        }

        problems.forEach(p -> System.out.println("PROBLEM " + p));
        System.out.println(problems.isEmpty() ? "All content checks pass." : problems.size() + " problem(s).");
        if (!problems.isEmpty()) {
            System.exit(1);
        }
    }

    private static String checkBlock(PracticeRunner runner, LessonCode.Block block) throws Exception {
        ProgramResult last = null;
        for (String program : LessonCode.candidatePrograms(block.code())) {
            last = runner.runSnippet(program);
            if (last.status() != ProgramResult.Status.COMPILE_ERROR) {
                break;
            }
        }
        if (last.status() == ProgramResult.Status.COMPILE_ERROR) {
            return "does not compile: " + last.compileErrors().getFirst().message().lines().findFirst().orElse("");
        }
        if (block.expectsCrash()) {
            return last.status() == ProgramResult.Status.EXCEPTION ? null : "crash snippet ended with " + last.status() + " instead of an exception";
        }
        if (block.runnable() && last.status() != ProgramResult.Status.COMPLETED) {
            return "runnable snippet ended with " + last.status();
        }
        return null;
    }

    /** The animation must parse, and a code animation's program must print exactly what its steps show. */
    private static String checkAnimation(PracticeRunner runner, dev.cadence.runner.NodeRunner node, LessonCode.Block block) throws Exception {
        Animation animation;
        try {
            animation = Animation.parse(block.code());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (animation.kind() != Animation.Kind.CODE) {
            return null;
        }
        if (animation.language().equals("js") || animation.language().equals("ts")) {
            ProgramResult run = animation.language().equals("ts")
                    ? node.runTypeScriptSnippet(animation.code(), "") : node.runSnippet(animation.code(), "");
            if (run.status() != ProgramResult.Status.COMPLETED) {
                return "animation code ended with " + run.status();
            }
            String actual = QuizSession.normalizeOutput(run.output());
            String expected = QuizSession.normalizeOutput(animation.expectedOutput());
            return actual.equals(expected) ? null : "animation shows output \"" + expected + "\" but the code prints \"" + actual + "\"";
        }
        ProgramResult last = null;
        for (String program : LessonCode.candidatePrograms(animation.code())) {
            last = runner.runSnippet(program);
            if (last.status() != ProgramResult.Status.COMPILE_ERROR) {
                break;
            }
        }
        if (last.status() == ProgramResult.Status.COMPILE_ERROR) {
            return "animation code does not compile: " + last.compileErrors().getFirst().message().lines().findFirst().orElse("");
        }
        if (last.status() != ProgramResult.Status.COMPLETED) {
            return "animation code ended with " + last.status();
        }
        String actual = QuizSession.normalizeOutput(last.output());
        String expected = QuizSession.normalizeOutput(animation.expectedOutput());
        return actual.equals(expected) ? null
                : "animation shows output \"" + expected + "\" but the code prints \"" + actual + "\"";
    }

    /** A ```js block must parse; a ```js run block must also finish, and a ```js run crash block must end in an error. */
    private static String checkJavaScript(dev.cadence.runner.NodeRunner node, LessonCode.Block block) throws Exception {
        var errors = node.check(block.code(), block.typeScript() ? "main.ts" : "main.js");
        if (!errors.isEmpty()) {
            return "JavaScript does not parse: line " + errors.getFirst().line() + ": " + errors.getFirst().message();
        }
        if (!block.runnable()) {
            return null;
        }
        ProgramResult result = block.typeScript() ? node.runTypeScriptSnippet(block.code(), "") : node.runSnippet(block.code(), "");
        if (block.expectsCrash()) {
            return result.status() == ProgramResult.Status.EXCEPTION ? null : "crash snippet ended with " + result.status() + " instead of an error";
        }
        return result.status() == ProgramResult.Status.COMPLETED ? null : "runnable snippet ended with " + result.status() + ": " + result.output().strip();
    }

    private static String checkJavaScriptPrediction(dev.cadence.runner.NodeRunner node, Quiz.PredictOutput predict, boolean ts)
            throws Exception {
        ProgramResult result = ts ? node.runTypeScriptSnippet(predict.code(), "") : node.runSnippet(predict.code(), "");
        if (result.status() != ProgramResult.Status.COMPLETED) {
            return "code does not run: " + result.status();
        }
        String actual = QuizSession.normalizeOutput(result.output());
        String expected = QuizSession.normalizeOutput(predict.answer());
        return actual.equals(expected) ? null : "answer is \"" + expected + "\" but the code prints \"" + actual + "\"";
    }

    private static String checkPrediction(PracticeRunner runner, Quiz.PredictOutput predict) throws Exception {
        for (String program : LessonCode.candidatePrograms(predict.code())) {
            ProgramResult result = runner.runSnippet(program);
            if (result.status() == ProgramResult.Status.COMPLETED) {
                String actual = QuizSession.normalizeOutput(result.output());
                String expected = QuizSession.normalizeOutput(predict.answer());
                return actual.equals(expected) ? null : "answer is \"" + expected + "\" but the code prints \"" + actual + "\"";
            }
        }
        return "code does not compile and run";
    }

    private static Set<String> curriculumIds(Path stateFile) throws Exception {
        var ids = new HashSet<String>();
        JsonNode state = JsonMapper.builder().build().readTree(stateFile.toFile());
        var lists = new ArrayList<JsonNode>();
        lists.add(state.get("modules"));
        if (state.has("tracks")) {
            for (JsonNode track : state.get("tracks")) {
                lists.add(track.get("modules"));
            }
        }
        for (JsonNode modules : lists) {
            for (JsonNode module : modules) {
                for (JsonNode topic : module.get("topics")) {
                    ids.add(topic.get("id").asString());
                }
            }
        }
        return ids;
    }
}
