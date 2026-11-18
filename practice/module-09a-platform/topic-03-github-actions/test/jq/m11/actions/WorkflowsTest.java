package jq.m11.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkflowsTest {

    @Test
    @DisplayName("An unquoted on: still gives its triggers, although SnakeYAML stores the key as the boolean true")
    void unquotedOnIsReadAsTrue() {
        String yaml = """
                name: CI
                on:
                  push:
                    branches: [main]
                  pull_request:
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    steps:
                      - run: ./mvnw -B verify
                """;
        assertEquals(List.of("push", "pull_request"), List.copyOf(Workflows.triggers(Workflows.load(yaml))),
                "the triggers are push and pull_request, in file order. YAML 1.1 reads the key on as true, "
                        + "so look under Boolean.TRUE when there is no \"on\" key");
    }

    @Test
    @DisplayName("on: can be one event, a list of events, or a quoted \"on\" key, and every form gives the trigger names")
    void everyShapeOfOnGivesTriggers() {
        String jobs = """
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    steps:
                      - run: ./mvnw -B verify
                """;
        assertEquals(Set.of("push"), Workflows.triggers(Workflows.load("on: push\n" + jobs)),
                "on: push is a single event name, so the triggers are just push");
        assertEquals(Set.of("push", "workflow_dispatch"), Workflows.triggers(Workflows.load("on: [push, workflow_dispatch]\n" + jobs)),
                "on: [push, workflow_dispatch] is a list, so both names are triggers");
        assertEquals(Set.of("schedule", "workflow_dispatch"),
                Workflows.triggers(Workflows.load("\"on\":\n  schedule:\n    - cron: '0 3 * * *'\n  workflow_dispatch:\n" + jobs)),
                "a quoted \"on\" stays the text on, so read it from the key \"on\" too");
        assertEquals(Set.of(), Workflows.triggers(Workflows.load("name: no triggers\n" + jobs)),
                "a workflow with no on: block has no triggers: return an empty set, not null");
    }

    @Test
    @DisplayName("Text that is not YAML, not a mapping, or has no jobs is rejected with InvalidWorkflowException")
    void badFilesAreRejected() {
        assertThrows(InvalidWorkflowException.class, () -> Workflows.load("- build\n- test\n"),
                "a list at the top is not a workflow: throw InvalidWorkflowException");
        assertThrows(InvalidWorkflowException.class, () -> Workflows.load("name: CI\non: push\n"),
                "a workflow without a jobs: mapping cannot run anything: throw InvalidWorkflowException");
        assertThrows(InvalidWorkflowException.class, () -> Workflows.load("jobs: [build\n"),
                "broken YAML (an unclosed [) should become InvalidWorkflowException, not a SnakeYAML exception");
    }
}
