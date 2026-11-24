package dev.cadence.practice;

import dev.cadence.content.Topic;
import dev.cadence.runner.CodeRunner;
import dev.cadence.runner.NodeRunner;
import dev.cadence.runner.PracticeRunner;
import java.util.Optional;

/** Picks the runner for a ticket: Java with javac and JUnit, or JavaScript and React with Node and Vitest. */
public final class Runners {

    private final PracticeRunner java;
    private final NodeRunner node;

    public Runners(PracticeRunner java, NodeRunner node) {
        this.java = java;
        this.node = node;
    }

    public CodeRunner forTopic(Topic topic) {
        return topic.meta().practice().runner().isJavaScript() ? node : java;
    }

    /** Lesson snippets: {@code js} runs with Node, everything else with Java. */
    public CodeRunner forLanguage(String language) {
        return "js".equals(language) || "javascript".equals(language) ? node : java;
    }

    public CodeRunner java() {
        return java;
    }

    /** The Node runner, while the web tools are installed (npm ci in the web workspace). */
    public Optional<NodeRunner> node() {
        return node.ready() ? Optional.of(node) : Optional.empty();
    }
}
