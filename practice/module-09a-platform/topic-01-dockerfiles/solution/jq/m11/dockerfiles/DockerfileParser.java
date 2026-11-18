package jq.m11.dockerfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Turns Dockerfile text into a list of instructions: comments dropped, continuation lines joined. */
public final class DockerfileParser {

    private DockerfileParser() {
    }

    public static List<Instruction> parse(String text) {
        List<Instruction> result = new ArrayList<>();
        String[] lines = text.split("\\R", -1);
        int startLine = 0;                       // 0 = not inside an instruction
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;                        // blank lines and comments, even inside a continuation
            }
            boolean continues = trimmed.endsWith("\\");
            if (continues) {
                trimmed = trimmed.substring(0, trimmed.length() - 1).strip();
            }
            if (startLine == 0) {
                startLine = i + 1;
            } else if (!trimmed.isEmpty()) {
                current.append(' ');
            }
            current.append(trimmed);
            if (!continues) {
                result.add(toInstruction(startLine, current.toString()));
                startLine = 0;
                current.setLength(0);
            }
        }
        if (startLine != 0 && !current.toString().isBlank()) {
            result.add(toInstruction(startLine, current.toString()));   // the file ended inside a continuation
        }
        return result;
    }

    private static Instruction toInstruction(int line, String text) {
        String stripped = text.strip();
        int space = indexOfWhitespace(stripped);
        String keyword = space < 0 ? stripped : stripped.substring(0, space);
        String args = space < 0 ? "" : stripped.substring(space).strip();
        return new Instruction(line, keyword.toUpperCase(Locale.ROOT), args);
    }

    private static int indexOfWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
