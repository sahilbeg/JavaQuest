package jq.m11.actions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

/** Reads a GitHub Actions workflow file with SnakeYAML. */
public final class Workflows {

    private Workflows() {
    }

    /** Loads the workflow text into maps and lists, and checks it has a top-level jobs mapping. */
    public static Map<?, ?> load(String yamlText) {
        Object root;
        try {
            root = new Yaml(new SafeConstructor(new LoaderOptions())).load(yamlText);
        } catch (YAMLException e) {
            throw new InvalidWorkflowException("this is not valid YAML: " + e.getMessage());
        }
        if (!(root instanceof Map<?, ?> workflow)) {
            throw new InvalidWorkflowException("a workflow must be a mapping of keys at the top, such as name:, on: and jobs:");
        }
        if (!(workflow.get("jobs") instanceof Map<?, ?> jobs) || jobs.isEmpty()) {
            throw new InvalidWorkflowException("a workflow needs a jobs: mapping with at least one job");
        }
        return workflow;
    }

    /**
     * The value under on:. YAML 1.1 reads an unquoted on as the boolean true, so SnakeYAML stores it
     * under the key Boolean.TRUE. A quoted "on" stays the text "on".
     */
    public static Object onBlock(Map<?, ?> workflow) {
        if (workflow.containsKey("on")) {
            return workflow.get("on");
        }
        return workflow.get(Boolean.TRUE);
    }

    /** The names of the events that start this workflow, in file order. */
    public static Set<String> triggers(Map<?, ?> workflow) {
        Object on = onBlock(workflow);
        Set<String> names = new LinkedHashSet<>();
        if (on instanceof String event) {
            names.add(event);
        } else if (on instanceof List<?> events) {
            for (Object event : events) {
                names.add(String.valueOf(event));
            }
        } else if (on instanceof Map<?, ?> events) {
            for (Object event : events.keySet()) {
                names.add(String.valueOf(event));
            }
        }
        return names;
    }
}
