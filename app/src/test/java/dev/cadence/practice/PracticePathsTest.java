package dev.cadence.practice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PracticePathsTest {

    private static final Path ROOT = Path.of("/repo/practice");

    @Test
    void sourceFileMapsToItsTopic() {
        assertEquals(Optional.of("module-01-basics/topic-01-variables"), PracticePaths.topicIdForSourceFile(ROOT,
                Path.of("/repo/practice/module-01-basics/topic-01-variables/src/jq/m01/variables/Variables.java")));
    }

    @Test
    void testAndSolutionFilesAreIgnored() {
        assertEquals(Optional.empty(), PracticePaths.topicIdForSourceFile(ROOT,
                Path.of("/repo/practice/module-01-basics/topic-01-variables/test/jq/VariablesTest.java")));
        assertEquals(Optional.empty(), PracticePaths.topicIdForSourceFile(ROOT,
                Path.of("/repo/practice/module-01-basics/topic-01-variables/solution/jq/Variables.java")));
    }

    @Test
    void nonJavaFilesInSrcAreIgnored() {
        assertEquals(Optional.empty(), PracticePaths.topicIdForSourceFile(ROOT,
                Path.of("/repo/practice/module-01-basics/topic-01-variables/src/Variables.java~")));
    }
}
