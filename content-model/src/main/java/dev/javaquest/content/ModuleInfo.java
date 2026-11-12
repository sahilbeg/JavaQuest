package dev.javaquest.content;

public record ModuleInfo(String id, String title, String project, String summary, String unlockAfter, String mascotIntro) {

    public ModuleInfo {
        if (id == null || !id.matches("module-\\d{2}-[a-z0-9-]+")) {
            throw new IllegalArgumentException("id must look like module-NN-name, got: " + id);
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
    }
}
