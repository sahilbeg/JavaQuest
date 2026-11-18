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
        // TODO 1: load the text with new Yaml(new SafeConstructor(new LoaderOptions())).load(yamlText).
        //         Turn a YAMLException into InvalidWorkflowException. Throw InvalidWorkflowException when the
        //         result is not a Map, or when its "jobs" value is not a Map with at least one job.
        return Map.of();
    }

    /**
     * The value under on:. YAML 1.1 reads an unquoted on as the boolean true, so SnakeYAML stores it
     * under the key Boolean.TRUE. A quoted "on" stays the text "on".
     */
    public static Object onBlock(Map<?, ?> workflow) {
        // TODO 2: return the value under the key "on" if there is one, otherwise the value under Boolean.TRUE
        return null;
    }

    /** The names of the events that start this workflow, in file order. */
    public static Set<String> triggers(Map<?, ?> workflow) {
        // TODO 3: onBlock(workflow) is a String (one event), a List (event names) or a Map (event name -> settings).
        //         Collect the event names into a LinkedHashSet. No on: block gives an empty set.
        return new LinkedHashSet<>();
    }
}
