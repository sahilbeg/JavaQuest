package dev.cadence.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LessonCodeTest {

    private static final String LESSON = """
            ## Concept

            ```java
            int a = 1;
            ```

            ```java run
            public class Main {
                public static void main(String[] args) {}
            }
            ```

            ```java nocompile
            int b
            ```

            ```mermaid
            flowchart LR
            ```
            """;

    @Test
    void findsJavaAndRunnableBlocksButSkipsNoCompileAndOtherLanguages() {
        var blocks = LessonCode.compilableBlocks(LESSON);

        assertEquals(List.of("java", "java run"), blocks.stream().map(LessonCode.Block::info).toList());
        assertEquals(3, blocks.getFirst().line());
    }

    @Test
    void wholeClassesAreCompiledAsWritten() {
        String code = "public class Kiosk {}\n";
        assertEquals(List.of(code), LessonCode.candidatePrograms(code));
    }

    @Test
    void fragmentsAreTriedAsStatementsThenAsMembers() {
        var programs = LessonCode.candidatePrograms("public static void main(String[] args) {\n}\n");

        assertEquals(2, programs.size());
        assertTrue(programs.get(0).contains("public static void main(String[] args) {\npublic static void main"));
        assertEquals(1, programs.get(1).split("void main", -1).length - 1, "member wrapper must not add a second main");
    }

    @Test
    void crashBlocksAreRunnableAndExpectAnException() {
        var blocks = LessonCode.compilableBlocks("```java run crash\nint[] a = new int[1];\n```\n");

        assertEquals(1, blocks.size());
        assertTrue(blocks.getFirst().runnable());
        assertTrue(blocks.getFirst().expectsCrash());
    }
}
