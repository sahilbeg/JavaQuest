package jq.m11.feature;

import java.util.List;
import java.util.NoSuchElementException;

/** A pipeline: a name and its stages, in the order they run. */
public record Pipeline(String name, List<Stage> stages) {

    public Pipeline {
        stages = List.copyOf(stages);
    }

    /** The stage with this name. */
    public Stage stage(String stageName) {
        return stages.stream()
                .filter(s -> s.name().equals(stageName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("no stage called " + stageName));
    }

    /** The last CANARY stage that comes before the given stage. */
    public Stage canaryBefore(Stage stage) {
        Stage canary = null;
        for (Stage s : stages) {
            if (s.equals(stage)) {
                break;
            }
            if (s.type() == StageType.CANARY) {
                canary = s;
            }
        }
        if (canary == null) {
            throw new NoSuchElementException("no canary stage before " + stage.name());
        }
        return canary;
    }
}
