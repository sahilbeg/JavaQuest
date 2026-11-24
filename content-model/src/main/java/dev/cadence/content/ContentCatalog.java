package dev.cadence.content;

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
    private static final List<String> REQUIRED_TOPIC_FILES = List.of("meta.yaml", "lesson.md");

    private final List<ModuleInfo> modules;
    private final List<Topic> orderedTopics;
    private final Map<String, Topic> topicsById;

    private ContentCatalog(List<ModuleInfo> modules, Map<String, Topic> topics) {
        this.modules = List.copyOf(modules);
        this.orderedTopics = List.copyOf(topics.values());
        this.topicsById = Map.copyOf(topics);
    }

    public static ContentCatalog load(Path contentDir, Path practiceDir) throws IOException {
        if (!Files.isDirectory(contentDir)) {
            throw new InvalidContentException(contentDir, "the content folder is missing. Start Cadence with ./run.sh from the repository folder");
        }
        var modules = new ArrayList<ModuleInfo>();
        var topics = new LinkedHashMap<String, Topic>();
        for (Path moduleDir : sortedSubdirectories(contentDir)) {
            ModuleInfo module = read(moduleDir.resolve("module.yaml"), ModuleInfo.class);
            requireMatchesFolder(module.id(), moduleDir.getFileName().toString(), moduleDir);
            modules.add(module);
            var moduleTopics = new ArrayList<Topic>();
            for (Path topicDir : sortedSubdirectories(moduleDir)) {
                moduleTopics.add(loadTopic(contentDir, practiceDir, topicDir));
            }
            moduleTopics.sort(Comparator.comparingInt((Topic t) -> t.meta().order()).thenComparing(Topic::id));
            moduleTopics.forEach(topic -> topics.put(topic.id(), topic));
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
        if (meta.practice().kind() == TopicMeta.PracticeKind.CODE) {
            for (String folder : List.of("src", "skeleton", "solution", "test")) {
                if (!Files.isDirectory(practice.resolve(folder))) {
                    throw new InvalidContentException(practice.resolve(folder), "a code topic needs this practice folder");
                }
            }
        }
        boolean quizless = meta.isBoss() || meta.isFeature();
        if (quizless) {
            if (Files.exists(topicDir.resolve("quiz.yaml"))) {
                throw new InvalidContentException(topicDir.resolve("quiz.yaml"), "incidents and features have no onboarding check");
            }
            if (meta.practice().kind() != TopicMeta.PracticeKind.CODE) {
                throw new InvalidContentException(topicDir.resolve("meta.yaml"), "incidents and features are graded by code checks");
            }
        }
        Quiz quiz = quizless ? null : read(topicDir.resolve("quiz.yaml"), Quiz.class);
        if (quiz != null && !quiz.topic().equals(meta.id())) {
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

    /** One catalog holding every pool's modules and topics, in the order given. A topic id may appear only once. */
    public static ContentCatalog merge(List<ContentCatalog> pools) {
        var modules = new ArrayList<ModuleInfo>();
        var topics = new LinkedHashMap<String, Topic>();
        for (ContentCatalog pool : pools) {
            modules.addAll(pool.modules);
            for (Topic topic : pool.orderedTopics) {
                if (topics.putIfAbsent(topic.id(), topic) != null) {
                    throw new InvalidContentException(topic.contentDir(), "topic id '" + topic.id() + "' is used in two content folders");
                }
            }
        }
        return new ContentCatalog(modules, topics);
    }

    /** A view with only these modules, in this order, and their topics. Ids that are not here yet are skipped. */
    public ContentCatalog subset(List<String> moduleIds) {
        var modules = new ArrayList<ModuleInfo>();
        var topics = new LinkedHashMap<String, Topic>();
        for (String id : moduleIds) {
            this.modules.stream().filter(m -> m.id().equals(id)).findFirst().ifPresent(module -> {
                modules.add(module);
                orderedTopics.stream().filter(t -> t.meta().moduleId().equals(id)).forEach(t -> topics.put(t.id(), t));
            });
        }
        return new ContentCatalog(modules, topics);
    }

    public static ContentCatalog empty() {
        return new ContentCatalog(List.of(), new LinkedHashMap<>());
    }

    public List<ModuleInfo> modules() {
        return modules;
    }

    public List<Topic> topics() {
        return orderedTopics;
    }

    public Optional<Topic> topic(String id) {
        return Optional.ofNullable(topicsById.get(id));
    }
}
