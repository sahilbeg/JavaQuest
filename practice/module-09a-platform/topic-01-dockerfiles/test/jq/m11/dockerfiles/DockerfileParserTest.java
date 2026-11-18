package jq.m11.dockerfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DockerfileParserTest {

    @Test
    @DisplayName("Continuation lines join into one instruction that keeps the line number where it started")
    void joinsContinuationLines() {
        String text = """
                from eclipse-temurin:21.0.8_9-jre AS run
                RUN apt-get update \\
                    && apt-get install -y --no-install-recommends tzdata \\
                    && rm -rf /var/lib/apt/lists/*
                USER 10001
                """;
        List<Instruction> parsed = DockerfileParser.parse(text);

        assertEquals(3, parsed.size(), "three instructions: FROM, one RUN spread over 3 lines, and USER. Got: " + parsed);
        assertEquals(new Instruction(1, "FROM", "eclipse-temurin:21.0.8_9-jre AS run"), parsed.get(0),
                "the keyword is turned into capitals (from -> FROM), and the arguments are the rest of the line, trimmed");
        assertEquals(new Instruction(2, "RUN",
                        "apt-get update && apt-get install -y --no-install-recommends tzdata && rm -rf /var/lib/apt/lists/*"),
                parsed.get(1),
                "drop each ending backslash, trim each piece, and join the pieces with one space. The RUN starts on line 2");
        assertEquals(new Instruction(5, "USER", "10001"), parsed.get(2),
                "USER is on line 5: line numbers count every line of the file, continuation lines included");
    }

    @Test
    @DisplayName("Comments and blank lines are skipped, even in the middle of a continued instruction")
    void skipsCommentsAndBlankLines() {
        String text = """
                # syntax=docker/dockerfile:1

                FROM eclipse-temurin:21.0.8_9-jre
                   # a comment with spaces in front is still a comment
                RUN groupadd --system --gid 10001 spoke \\
                # make the user too
                 && useradd --system --uid 10001 --gid 10001 spoke

                ENTRYPOINT ["java", "-jar", "spoke-api.jar"]
                """;
        List<Instruction> parsed = DockerfileParser.parse(text);

        assertEquals(List.of("FROM", "RUN", "ENTRYPOINT"), parsed.stream().map(Instruction::keyword).toList(),
                "only real instructions are kept: lines whose first non-space character is # are comments");
        assertEquals(new Instruction(5, "RUN",
                        "groupadd --system --gid 10001 spoke && useradd --system --uid 10001 --gid 10001 spoke"),
                parsed.get(1),
                "a comment line inside a continuation is skipped, and the instruction carries on with the next line");
        assertEquals(3, parsed.get(0).line(), "FROM is on line 3, after the comment on line 1 and the blank line 2");
        assertEquals(9, parsed.get(2).line(), "ENTRYPOINT is on line 9");
    }

    @Test
    @DisplayName("An empty file, or a file of only comments, has no instructions")
    void emptyAndCommentOnlyFiles() {
        assertTrue(DockerfileParser.parse("").isEmpty(), "an empty Dockerfile has no instructions");
        assertTrue(DockerfileParser.parse("# just a note\n\n   # another note\n").isEmpty(),
                "a Dockerfile of only comments and blank lines has no instructions");
        assertEquals(List.of(new Instruction(2, "CMD", "[\"java\", \"-version\"]")),
                DockerfileParser.parse("\nCMD [\"java\", \"-version\"]"),
                "a last line with no line break after it still counts, and the JSON form stays as written");
    }
}
