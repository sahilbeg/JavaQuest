package dev.cadence.content;

public record ModuleInfo(String id, String title, String project, String summary, String unlockAfter, String mascotIntro) {

    /** A pool prefix (module, web, react...), two digits, an optional letter, then a name: module-01-basics, web-03-javascript-basics. */
    public static final String ID_PATTERN = "[a-z]+-\\d{2}[a-z]?-[a-z0-9-]+";

    public ModuleInfo {
        if (id == null || !id.matches(ID_PATTERN)) {
            throw new IllegalArgumentException("id must look like module-NN-name or web-NN-name (a pool prefix, a 2-digit number, an optional"
                    + " letter for a module inserted later, then a name), got: " + id);
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
    }
}
