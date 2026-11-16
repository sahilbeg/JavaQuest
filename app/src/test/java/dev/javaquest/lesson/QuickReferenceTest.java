package dev.javaquest.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuickReferenceTest {

    private final QuickReference reference = new QuickReference();

    @Test
    @DisplayName("Notes take the recap, syntax boxes, new words and common mistakes from a lesson")
    void takesEachPartOfTheLesson() {
        var notes = reference.notes("""
                ## Hook

                A story.

                ## Concept

                ```callout jargon
                A **variable** is a labelled box.
                ```

                ### Syntax: declaring a `String`

                ```java
                String bikeId;
                ```

                To **declare** means to create the box.

                A second paragraph that is not part of the note.

                ### Syntax: a box with no explanation

                ```java nocompile
                int docks = 12;
                ```

                ### Naming rules

                ```java
                int notSyntax = 1;
                ```

                ## Recap

                - A variable has a type and a name.
                - Use `int` for whole numbers.

                ## Common mistakes

                **1. Using `==` on Strings.**

                ```java nocompile
                if (a == b) {}
                ```

                It compares references.

                Use `a.equals(b)`.

                **2. Forgetting the semicolon.**

                Java says: `';' expected`.
                """);

        assertTrue(notes.keyPointsHtml().startsWith("<ul>"), "The recap list should become the key points");
        assertTrue(notes.keyPointsHtml().contains("<code>int</code>"), "Inline code in the recap should stay code");

        assertEquals(2, notes.syntax().size(), "Only '### Syntax:' boxes are syntax notes");
        assertEquals("declaring a <code>String</code>", notes.syntax().get(0).titleHtml());
        assertEquals("String bikeId;", notes.syntax().get(0).code());
        assertTrue(notes.syntax().get(0).noteHtml().contains("<strong>declare</strong>"), "The first paragraph explains the box");
        assertFalse(notes.syntax().get(0).noteHtml().contains("second paragraph"), "Only the first paragraph is kept");
        assertEquals("", notes.syntax().get(1).noteHtml(), "A box without a paragraph still keeps its code");

        assertEquals(1, notes.wordsHtml().size());
        assertTrue(notes.wordsHtml().get(0).contains("<strong>variable</strong>"));

        assertEquals(2, notes.mistakes().size());
        assertEquals("Using <code>==</code> on Strings.", notes.mistakes().get(0).titleHtml(), "The number is dropped from the title");
        assertEquals("if (a == b) {}", notes.mistakes().get(0).code());
        assertTrue(notes.mistakes().get(0).fixHtml().contains("a.equals(b)"), "Every paragraph up to the next mistake is the fix");
        assertEquals("", notes.mistakes().get(1).code(), "A mistake without code has none");
        assertTrue(notes.mistakes().get(1).fixHtml().contains("expected"));
    }

    @Test
    @DisplayName("Text in a lesson is escaped, never turned into HTML")
    void escapesHtml() {
        var notes = reference.notes("## Recap\n\n- Use <b>bold</b> carefully.\n");
        assertFalse(notes.keyPointsHtml().contains("<b>"), "Raw HTML in a lesson must be escaped");
    }

    @Test
    @DisplayName("Every lesson in the course has key points in its quick reference")
    void everyLessonHasKeyPoints() throws IOException {
        Path content = Path.of("..", "content");
        List<Path> lessons;
        try (Stream<Path> files = Files.walk(content)) {
            lessons = files.filter(p -> p.getFileName().toString().equals("lesson.md")).sorted().toList();
        }
        assertFalse(lessons.isEmpty(), "No lessons found under " + content.toAbsolutePath());
        for (Path lesson : lessons) {
            var notes = reference.notes(Files.readString(lesson));
            assertTrue(notes.keyPointsHtml().contains("<li>"), lesson + " should have Recap bullets for its key points");
            for (var syntax : notes.syntax()) {
                assertFalse(syntax.code().isBlank(), lesson + ": syntax box \"" + syntax.titleHtml() + "\" has no code");
            }
            for (var mistake : notes.mistakes()) {
                assertFalse(mistake.titleHtml().isBlank(), lesson + " has a common mistake without a title");
                assertFalse(mistake.fixHtml().isBlank() && mistake.code().isBlank(),
                        lesson + ": common mistake \"" + mistake.titleHtml() + "\" has neither code nor a fix");
            }
        }
    }
}
