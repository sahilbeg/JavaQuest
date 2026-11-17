package dev.javaquest.practice;

import static org.assertj.core.api.Assertions.assertThat;

import dev.javaquest.content.Topic;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The VS Code walkthrough points at the real folder, and lists the files with work to do first. */
class CodeGuideTest {

    @TempDir
    Path practice;

    private final PracticeService service = new PracticeService(null, null, null, null);

    private static void write(Path file, String text) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, text);
    }

    @Test
    @DisplayName("Files with TODOs come first, with their numbers and the line of the first TODO left")
    void listsTodoFilesFirst() throws IOException {
        Path unit = practice.resolve("module-02-oop/topic-02-constructors");
        String pkg = "jq/m02/constructors/";
        write(unit.resolve("README.md"), "# Constructors");
        write(unit.resolve("skeleton/" + pkg + "Bike.java"), "class Bike {\n// TODO 1: a\n// TODO 2: b\n}\n");
        write(unit.resolve("skeleton/" + pkg + "Helper.java"), "class Helper {}\n");
        write(unit.resolve("skeleton/" + pkg + "Station.java"), "class Station {\n// TODO 3: c\n}\n");
        // The learner already finished TODO 1 in Bike and all of Station.
        write(unit.resolve("src/" + pkg + "Bike.java"), "class Bike {\nint id;\n\n// TODO 2: b\n}\n");
        write(unit.resolve("src/" + pkg + "Helper.java"), "class Helper {}\n");
        write(unit.resolve("src/" + pkg + "Station.java"), "class Station {\nint docks;\n}\n");

        CodeGuide guide = service.codeGuide(new Topic(null, null, practice, unit)).orElseThrow();

        assertThat(guide.practiceRoot()).as("VS Code opens the practice folder").isEqualTo(practice.toAbsolutePath().normalize().toString());
        assertThat(guide.folder()).endsWith("topic-02-constructors");
        assertThat(guide.readme()).endsWith("README.md");
        assertThat(guide.files()).extracting(CodeGuide.GuideFile::path)
                .as("files with work to do come first")
                .containsExactly(pkg + "Bike.java", pkg + "Station.java", pkg + "Helper.java");
        CodeGuide.GuideFile bike = guide.files().getFirst();
        assertThat(bike.todos()).as("TODO numbers come from the starting code").isEqualTo(List.of(1, 2));
        assertThat(bike.line()).as("jump to the first TODO still in the learner's file").isEqualTo(4);
        assertThat(guide.files().get(1).line()).as("no TODO left means line 1").isEqualTo(1);
        assertThat(guide.files().get(2).toChange()).as("a file without TODOs needs no change").isFalse();
        assertThat(bike.absolutePath()).isEqualTo(unit.resolve("src/" + pkg + "Bike.java").toAbsolutePath().normalize().toString());
    }

    @Test
    @DisplayName("A ticket without code has no walkthrough")
    void noCodeNoGuide() throws IOException {
        Path unit = practice.resolve("module-00-foundations/topic-01-how-computers-work");
        assertThat(service.codeGuide(new Topic(null, null, practice, unit))).isEmpty();
    }
}
