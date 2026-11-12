package dev.javaquest.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Finds the Java code blocks in a lesson and turns fragments into programs the compiler accepts. */
public final class LessonCode {

    public record Block(int line, String info, String code) {
        public boolean runnable() {
            return info.equals("java run");
        }
    }

    private static final Pattern FENCE = Pattern.compile("^```([^\\n]*)\\n(.*?)^```", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern TOP_LEVEL_TYPE = Pattern.compile("^(public\\s+)?(final\\s+)?(class|record|enum|interface)\\s", Pattern.MULTILINE);

    private LessonCode() {}

    /** Java blocks that must compile: plain ```java and ```java run, but not ```java nocompile. */
    public static List<Block> compilableBlocks(String markdown) {
        var blocks = new ArrayList<Block>();
        var matcher = FENCE.matcher(markdown);
        while (matcher.find()) {
            String info = matcher.group(1).strip();
            if (info.equals("java") || info.equals("java run")) {
                int line = (int) markdown.substring(0, matcher.start()).lines().count() + 1;
                blocks.add(new Block(line, info, matcher.group(2)));
            }
        }
        return blocks;
    }

    /** The ways a block could be a whole program: as written, as statements in main, or as members of a class. */
    public static List<String> candidatePrograms(String code) {
        if (TOP_LEVEL_TYPE.matcher(code).find()) {
            return List.of(code);
        }
        return List.of(
                "public class Main {\n    public static void main(String[] args) {\n" + code + "\n    }\n}\n",
                "public class Main {\n" + code + "\n}\n");
    }
}
