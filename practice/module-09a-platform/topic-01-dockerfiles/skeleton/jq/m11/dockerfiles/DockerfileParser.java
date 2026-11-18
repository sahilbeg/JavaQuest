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
            // TODO 1: skip blank lines and lines that start with # (continue), even in the middle of a continuation.
            // TODO 2: if the line ends with a backslash, remove it (and strip again): the instruction goes on.
            //         Remember the 1-based line number where an instruction starts (startLine = i + 1).
            //         Append each piece to current, with one space between pieces.
            //         When a line does not end with a backslash, the instruction is complete:
            //         result.add(toInstruction(startLine, current.toString())), then reset startLine and current.
        }
        // TODO 3: if the file ended in the middle of a continuation, add that last instruction too.
        return result;
    }

    /** Given: splits "run echo hi" into keyword RUN (capitals) and args "echo hi". */
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
