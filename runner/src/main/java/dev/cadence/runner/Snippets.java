package dev.cadence.runner;

import java.util.regex.Pattern;

public final class Snippets {

    private static final Pattern TOP_LEVEL_CLASS =
            Pattern.compile("^(?:public\\s+)?(?:final\\s+)?(?:class|record|enum|interface)\\s+([A-Za-z_$][\\w$]*)",
                    Pattern.MULTILINE);

    private Snippets() {}

    public static String mainClassName(String code) {
        var matcher = TOP_LEVEL_CLASS.matcher(code);
        if (!matcher.find()) {
            throw new IllegalArgumentException("The snippet has no top-level class");
        }
        return matcher.group(1);
    }
}
