package dev.javaquest.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

/** Loads every module and topic under content/ once and fails with the file path when a file is invalid. */
public final class ContentCatalog {

    private static final YAMLMapper YAML = YAMLMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private static final List<String> REQUIRED_TOPIC_FILES = List.of("meta.yaml", "lesson.md", "quiz.yaml");

    private final List<ModuleInfo> modules;
    private final Map<String, Topic> topics;

    private ContentCatalog(List<ModuleInfo> modules, Map<String, Topic> topics) {
        this.modules = List.copyOf(modules);
        this.topics = Map.copyOf(topics);
    }

    public static ContentCatalog load(Path contentDir, Path practiceDir) throws IOException {
        var modules = new ArrayList<ModuleInfo>();
        var topics = new LinkedHashMap<String, Topic>();
        for (Path moduleDir : sortedSubdirectories(contentDir)) {
            ModuleInfo module = read(moduleDir.resolve("module.yaml"), ModuleInfo.class);
            requireMatchesFolder(module.id(), moduleDir.getFileName().toString(), moduleDir);
            modules.add(module);
            for (Path topicDir : sortedSubdirectories(moduleDir)) {
                Topic topic = loadTopic(contentDir, practiceDir, topicDir);
                topics.put(topic.id(), topic);
            }
        }
        return new ContentCatalog(modules, topics);
    }

    private static Topic loadTopic(Path contentDir, Path practiceDir, Path topicDir) {
        for (String file : REQUIRED_TOPIC_FILES) {
            if (!Files.isRegularFile(topicDir.resolve(file))) {
                throw new InvalidContentException(topicDir.resolve(file), "file is missing");
            }
        }
        TopicMeta meta = read(topicDir.resolve("meta.yaml"), TopicMeta.class);
        String folderId = contentDir.relativize(topicDir).toString().replace('\\', '/');
        requireMatchesFolder(meta.id(), folderId, topicDir);
        Path practice = practiceDir.resolve(meta.id());
        if (meta.practice().kind() == TopicMeta.PracticeKind.CODE && !Files.isDirectory(practice.resolve("test"))) {
            throw new InvalidContentException(practice.resolve("test"), "a code topic needs practice tests");
        }
        Quiz quiz = read(topicDir.resolve("quiz.yaml"), Quiz.class);
        if (!quiz.topic().equals(meta.id())) {
            throw new InvalidContentException(topicDir.resolve("quiz.yaml"), "topic '" + quiz.topic() + "' does not match '" + meta.id() + "'");
        }
        return new Topic(meta, quiz, topicDir, practice);
    }

    private static <T> T read(Path file, Class<T> type) {
        if (!Files.isRegularFile(file)) {
            throw new InvalidContentException(file, "file is missing");
        }
        try {
            return YAML.readValue(file.toFile(), type);
        } catch (JacksonException e) {
            Throwable cause = e.getCause() instanceof IllegalArgumentException invalid ? invalid : e;
            throw new InvalidContentException(file, cause.getMessage());
        }
    }

    private static void requireMatchesFolder(String id, String folderId, Path dir) {
        if (!id.equals(folderId)) {
            throw new InvalidContentException(dir, "id '" + id + "' does not match its folder '" + folderId + "'");
        }
    }

    private static List<Path> sortedSubdirectories(Path dir) throws IOException {
        try (Stream<Path> children = Files.list(dir)) {
            return children.filter(Files::isDirectory).sorted(Comparator.comparing(Path::getFileName)).toList();
        }
    }

    public List<ModuleInfo> modules() {
        return modules;
    }

    public List<Topic> topics() {
        return topics.values().stream().toList();
    }

    public Optional<Topic> topic(String id) {
        return Optional.ofNullable(topics.get(id));
    }
}
