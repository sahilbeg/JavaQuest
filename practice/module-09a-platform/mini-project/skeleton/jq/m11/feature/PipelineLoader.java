package jq.m11.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

/** Reads a pipeline from YAML and checks it, so a bad file fails before anything runs. */
public final class PipelineLoader {

    private PipelineLoader() {
    }

    public static Pipeline load(String yaml) {
        // TODO 1: refuse an empty file, then parse it with new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml).
        // Wrap a YAMLException in a PipelineException whose message contains "YAML".
        // TODO 2: the top level must be a Map with a non-blank String name and a non-empty List of stages.
        // TODO 3: turn each stage map into a Stage with the README's rules: name, unique name, known type,
        // canary percent 1..50, approval expiresAfterMinutes >= 1, analysis thresholds and a canary before it.
        // Every problem throws PipelineException with a message that names the stage and the key.
        return new Pipeline("", List.of());
    }
}
