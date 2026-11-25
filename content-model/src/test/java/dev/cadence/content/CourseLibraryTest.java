package dev.cadence.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CourseLibraryTest {

    private static final Path REPO = Path.of("..").toAbsolutePath().normalize();

    @Test
    void theJavaCourseIsExactlyTheContentFolder() throws Exception {
        var library = CourseLibrary.load(REPO);
        var java = library.catalog("java-swe");
        var direct = ContentCatalog.load(REPO.resolve("content"), REPO.resolve("practice"));
        assertEquals(direct.topics().stream().map(Topic::id).toList(), java.topics().stream().map(Topic::id).toList());
        assertEquals(CourseLibrary.Status.READY, library.course("java-swe").orElseThrow().status());
    }

    @Test
    void aPlannedCourseShowsItsWholePathBeforeItIsWritten() throws Exception {
        var library = CourseLibrary.load(REPO);
        var path = library.path("frontend-react");
        assertEquals(11, path.size());
        assertEquals("web-00-how-the-web-works", path.getFirst().id());
        assertEquals("Frontend Intern", library.course("frontend-react").orElseThrow().ladder().getFirst());
    }

    @Test
    void aTesterKeepsTicketCodeInItsOwnFolderStartingFromTheSkeleton(@TempDir Path code) throws Exception {
        var library = CourseLibrary.load(REPO, code);
        Topic java = library.library().topic("module-01-basics/topic-04-console-io").orElseThrow();
        Topic web = library.library().topic("web-03-javascript-basics/topic-01-variables").orElseThrow();

        assertEquals(code.resolve("java/module-01-basics/topic-04-console-io/src"), java.practiceSources());
        assertEquals(code.resolve("web/web-03-javascript-basics/topic-01-variables/src"), web.practiceSources());
        assertEquals(REPO.resolve("practice/module-01-basics/topic-04-console-io/skeleton"), java.practiceSkeleton());
        Path copied = java.practiceSources().resolve("jq/m01/consoleio/KioskPrompt.java");
        assertEquals(Files.readString(java.practiceSkeleton().resolve("jq/m01/consoleio/KioskPrompt.java")), Files.readString(copied));

        // A second start keeps the tester's edits.
        Files.writeString(copied, "// edited by the tester");
        CourseLibrary.load(REPO, code);
        assertEquals("// edited by the tester", Files.readString(copied));
    }

    @Test
    void everyTopicCanBeFoundInTheLibrary() throws Exception {
        var library = CourseLibrary.load(REPO);
        assertTrue(library.library().topic("module-01-basics/topic-04-console-io").isPresent());
    }

    @Test
    void aCourseWithAnUnknownObjectiveIsRefused(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("courses.yaml"), """
                pools: []
                objectives: [{ id: frontend, title: Frontend }]
                courses:
                  - { id: x, objective: backend, title: X, status: soon, ladder: [A, B] }
                """);
        var error = assertThrows(InvalidContentException.class, () -> CourseLibrary.load(root));
        assertTrue(error.getMessage().contains("unknown objective"), error.getMessage());
    }

    @Test
    void aReadyCourseMustHaveTopics(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("courses.yaml"), """
                pools: []
                objectives: [{ id: frontend, title: Frontend }]
                courses:
                  - { id: x, objective: frontend, title: X, status: ready, modules: [{ id: web-00-a, title: A }], ladder: [A, B] }
                """);
        var error = assertThrows(InvalidContentException.class, () -> CourseLibrary.load(root));
        assertTrue(error.getMessage().contains("no topics"), error.getMessage());
    }
}
