package dev.javaquest.content;

import java.nio.file.Path;

public record Topic(TopicMeta meta, Quiz quiz, Path contentDir, Path practiceDir) {

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
