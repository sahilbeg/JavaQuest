package dev.cadence.content;

import java.nio.file.Path;

/**
 * @param sourcesDir where the learner's code for this ticket lives: practiceDir/src in a learner's own copy, or a
 *                   folder in the tester's or author's data folder, so test runs never touch a learner's code
 */
public record Topic(TopicMeta meta, Quiz quiz, Path contentDir, Path practiceDir, Path sourcesDir) {

    public Topic(TopicMeta meta, Quiz quiz, Path contentDir, Path practiceDir) {
        this(meta, quiz, contentDir, practiceDir, practiceDir.resolve("src"));
    }

    /** The same ticket, with its code kept in another folder. */
    public Topic withSources(Path sources) {
        return new Topic(meta, quiz, contentDir, practiceDir, sources);
    }

    /** Bosses and features have no onboarding check; their checks are the whole task. */
    public boolean hasQuiz() {
        return quiz != null;
    }

    public int passPercent() {
        return hasQuiz() ? quiz.passPercent() : 100;
    }

    public String id() {
        return meta.id();
    }

    public Path lessonFile() {
        return contentDir.resolve("lesson.md");
    }

    public Path practiceSources() {
        return sourcesDir;
    }

    public Path practiceSkeleton() {
        return practiceDir.resolve("skeleton");
    }

    public Path practiceSolution() {
        return practiceDir.resolve("solution");
    }

    public Path practiceTests() {
        return practiceDir.resolve("test");
    }
}
