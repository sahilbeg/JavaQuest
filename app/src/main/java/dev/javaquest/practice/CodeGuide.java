package dev.javaquest.practice;

import java.util.List;

/**
 * What the VS Code walkthrough needs to point at real places on this machine: the practice folder to open, the
 * ticket's folder and brief, and each source file with the TODOs it holds.
 *
 * @param practiceRoot the practice folder, which VS Code opens as the project (its pom.xml finds JUnit and Spring)
 * @param folder       this ticket's folder inside it
 * @param readme       the ticket's brief, README.md, or null when there is none
 * @param files        the files in src/, the ones with work to do first
 */
public record CodeGuide(String practiceRoot, String folder, String readme, List<GuideFile> files) {

    /**
     * @param path         the path inside src/, such as jq/m02/constructors/Bike.java
     * @param absolutePath the file on disk, for the vscode:// link
     * @param todos        the TODO numbers the starting code gives this file, in order
     * @param line         the line of the first TODO still in the learner's file, or 1
     * @param toChange     true when the starting code has a TODO here, so the ticket needs a change in it
     */
    public record GuideFile(String path, String absolutePath, List<Integer> todos, int line, boolean toChange) {}
}
