package dev.cadence.content;

import java.nio.file.Path;

public record Topic(TopicMeta meta, Quiz quiz, Path contentDir, Path practiceDir) {

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
        return practiceDir.resolve("src");
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
