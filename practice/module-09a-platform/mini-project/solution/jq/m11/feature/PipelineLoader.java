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
        if (yaml == null || yaml.isBlank()) {
            throw new PipelineException("the pipeline file is empty");
        }
        Object root;
        try {
            root = new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml);
        } catch (YAMLException e) {
            throw new PipelineException("the pipeline file is not valid YAML: " + e.getMessage(), e);
        }
        if (!(root instanceof Map<?, ?> top)) {
            throw new PipelineException("the top level must be a map with name and stages");
        }
        if (!(top.get("name") instanceof String name) || name.isBlank()) {
            throw new PipelineException("the pipeline needs a name");
        }
        if (!(top.get("stages") instanceof List<?> rawStages) || rawStages.isEmpty()) {
            throw new PipelineException("the pipeline needs a non-empty list of stages");
        }

        List<Stage> stages = new ArrayList<>();
        Set<String> names = new HashSet<>();
        boolean canarySeen = false;
        for (int i = 0; i < rawStages.size(); i++) {
            String where = "stage " + (i + 1);
            if (!(rawStages.get(i) instanceof Map<?, ?> raw)) {
                throw new PipelineException(where + " must be a map with a name and a type");
            }
            if (!(raw.get("name") instanceof String stageName) || stageName.isBlank()) {
                throw new PipelineException(where + " needs a name");
            }
            where = "stage " + stageName;
            if (!names.add(stageName)) {
                throw new PipelineException("two stages are called " + stageName + "; stage names must be unique");
            }
            if (!(raw.get("type") instanceof String typeText)) {
                throw new PipelineException(where + " needs a type");
            }
            StageType type = typeOf(typeText, where);
            Stage stage = switch (type) {
                case CANARY -> {
                    canarySeen = true;
                    yield Stage.canary(stageName, wholeNumber(raw, "percent", 1, 50, where));
                }
                case APPROVAL -> Stage.approval(stageName, wholeNumber(raw, "expiresAfterMinutes", 1, Integer.MAX_VALUE, where));
                case ANALYSIS -> {
                    if (!canarySeen) {
                        throw new PipelineException(where + " is an analysis, so a canary stage must come before it");
                    }
                    if (!(raw.get("maxErrorRateIncrease") instanceof Number rate)
                            || rate.doubleValue() < 0 || !(rate instanceof Integer || rate instanceof Double)) {
                        throw new PipelineException(where + " needs maxErrorRateIncrease, a number of 0 or more");
                    }
                    yield Stage.analysis(stageName, rate.doubleValue(),
                            wholeNumber(raw, "maxP99IncreasePercent", 0, Integer.MAX_VALUE, where));
                }
                default -> Stage.of(stageName, type);
            };
            stages.add(stage);
        }
        return new Pipeline(name, stages);
    }

    private static StageType typeOf(String text, String where) {
        for (StageType type : StageType.values()) {
            if (type.name().toLowerCase(Locale.ROOT).equals(text)) {
                return type;
            }
        }
        throw new PipelineException(where + " has an unknown type: " + text);
    }

    private static int wholeNumber(Map<?, ?> raw, String key, int min, int max, String where) {
        if (raw.get(key) instanceof Integer value && value >= min && value <= max) {
            return value;
        }
        String range = max == Integer.MAX_VALUE ? min + " or more" : "from " + min + " to " + max;
        throw new PipelineException(where + " needs " + key + ", a whole number " + range);
    }
}
