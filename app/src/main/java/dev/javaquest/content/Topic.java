package dev.javaquest.content;

import java.nio.file.Path;

public record Topic(TopicMeta meta, Path contentDir, Path practiceDir) {

    public String id() {
        return meta.id();
    }

    public Path practiceSources() {
        return practiceDir.resolve("src");
    }

    public Path practiceTests() {
        return practiceDir.resolve("test");
    }
}
