package dev.javaquest.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ContentCatalogTest {

    private static final String META = """
            id: module-01-basics/topic-01-variables
            title: Variables
            order: 1
            minutes: 8
            practice: { kind: code }
            ticket: { key: SPK-0101, from: maya, title: Store the ride length, body: Riders see 0 minutes. }
            hints: [one, two, three]
            review: true
            """;

    private static final String QUIZ = """
            topic: module-01-basics/topic-01-variables
            drawCount: 1
            passPercent: 80
            questions:
              - { id: q1, type: mcq, concept: c, prompt: P, choices: [a, b], answer: 0, explain: E }
            """;

    @TempDir
    Path root;
    Path content;
    Path practice;
    Path topicDir;

    @BeforeEach
    void writeValidTopic() throws IOException {
        content = root.resolve("content");
        practice = root.resolve("practice");
        Path moduleDir = Files.createDirectories(content.resolve("module-01-basics"));
        Files.writeString(moduleDir.resolve("module.yaml"), "id: module-01-basics\ntitle: Java basics\n");
        topicDir = Files.createDirectories(moduleDir.resolve("topic-01-variables"));
        Files.writeString(topicDir.resolve("meta.yaml"), META);
        Files.writeString(topicDir.resolve("lesson.md"), "## Hook\n");
        Files.writeString(topicDir.resolve("quiz.yaml"), QUIZ);
        for (String folder : List.of("src", "skeleton", "solution", "test")) {
            Files.createDirectories(practice.resolve("module-01-basics/topic-01-variables").resolve(folder));
        }
    }

    @Test
    void loadsAValidTopic() throws IOException {
        var catalog = ContentCatalog.load(content, practice);

        Topic topic = catalog.topic("module-01-basics/topic-01-variables").orElseThrow();
        assertEquals("Variables", topic.meta().title());
        assertEquals(TopicMeta.RunnerKind.PLAIN, topic.meta().practice().runner());
    }

    @Test
    void missingLessonNamesTheFile() throws IOException {
        Files.delete(topicDir.resolve("lesson.md"));

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("lesson.md"), error.getMessage());
    }

    @Test
    void wrongHintCountIsRejectedWithTheReason() throws IOException {
        Files.writeString(topicDir.resolve("meta.yaml"), META.replace("[one, two, three]", "[one, two]"));

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("exactly 3 hints"), error.getMessage());
    }

    @Test
    void misspelledFieldIsRejected() throws IOException {
        Files.writeString(topicDir.resolve("meta.yaml"), META.replace("minutes:", "minuts:"));

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("minutes"), error.getMessage());
    }

    @Test
    void badTicketKeyIsRejectedWithTheReason() throws IOException {
        Files.writeString(topicDir.resolve("meta.yaml"), META.replace("SPK-0101", "TICKET-1"));

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("ticket.key"), error.getMessage());
    }

    @Test
    void quizForAnotherTopicIsRejected() throws IOException {
        Files.writeString(topicDir.resolve("quiz.yaml"), QUIZ.replace("topic-01-variables", "topic-09-other"));

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("does not match"), error.getMessage());
    }

    @Test
    void unknownExtraFieldIsRejected() throws IOException {
        Files.writeString(topicDir.resolve("meta.yaml"), META + "colour: red\n");

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("colour"), error.getMessage());
    }

    @Test
    void idThatDoesNotMatchItsFolderIsRejected() throws IOException {
        Files.writeString(topicDir.resolve("meta.yaml"), META.replace("topic-01-variables", "topic-02-variables"));

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("does not match its folder"), error.getMessage());
    }

    @Test
    void codeTopicWithoutASkeletonIsRejected() throws IOException {
        Files.delete(practice.resolve("module-01-basics/topic-01-variables/skeleton"));

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("skeleton"), error.getMessage());
    }

    @Test
    void topicsFollowTheirOrderFieldNotTheirFolderNames() throws IOException {
        Path boss = Files.createDirectories(content.resolve("module-01-basics/boss"));
        Files.writeString(boss.resolve("meta.yaml"), META.replace("module-01-basics/topic-01-variables", "module-01-basics/boss")
                .replace("order: 1", "order: 99").replace("SPK-0101", "INC-01"));
        Files.writeString(boss.resolve("lesson.md"), "## Hook\n");
        for (String folder : List.of("src", "skeleton", "solution", "test")) {
            Files.createDirectories(practice.resolve("module-01-basics/boss").resolve(folder));
        }

        var ids = ContentCatalog.load(content, practice).topics().stream().map(Topic::id).toList();

        assertEquals(List.of("module-01-basics/topic-01-variables", "module-01-basics/boss"), ids);
    }

    @Test
    void aModuleInsertedLaterSortsBetweenItsNeighbours() throws IOException {
        for (String module : List.of("module-02-oop", "module-01a-tools")) {
            Path dir = Files.createDirectories(content.resolve(module));
            Files.writeString(dir.resolve("module.yaml"), "id: " + module + "\ntitle: " + module + "\n");
        }

        var ids = ContentCatalog.load(content, practice).modules().stream().map(ModuleInfo::id).toList();

        assertEquals(List.of("module-01-basics", "module-01a-tools", "module-02-oop"), ids);
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleInfo("module-1a-tools", "Tools", null, null, null, null));
    }

    @Test
    void incidentWithAQuizIsRejected() throws IOException {
        Path boss = Files.createDirectories(content.resolve("module-01-basics/boss"));
        Files.writeString(boss.resolve("meta.yaml"), META.replace("module-01-basics/topic-01-variables", "module-01-basics/boss")
                .replace("SPK-0101", "INC-01"));
        Files.writeString(boss.resolve("lesson.md"), "## Hook\n");
        Files.writeString(boss.resolve("quiz.yaml"), QUIZ);
        for (String folder : List.of("src", "skeleton", "solution", "test")) {
            Files.createDirectories(practice.resolve("module-01-basics/boss").resolve(folder));
        }

        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(content, practice));
        assertTrue(error.getMessage().contains("no onboarding check"), error.getMessage());
    }

    @Test
    void missingContentFolderSaysHowToStart() {
        var error = assertThrows(InvalidContentException.class, () -> ContentCatalog.load(root.resolve("nowhere"), practice));
        assertTrue(error.getMessage().contains("./run.sh"), error.getMessage());
    }
}
