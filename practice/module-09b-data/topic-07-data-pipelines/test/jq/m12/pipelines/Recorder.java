package jq.m12.pipelines;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fake task actions for the checks. Each call is written down as "task date", such as "extract_rides 2026-11-17". */
final class Recorder {

    final List<String> calls = new ArrayList<>();
    private final Set<String> broken = new HashSet<>();

    /** An action that succeeds, unless the task has been broken with breakTask. */
    TaskAction task(String name) {
        return date -> {
            calls.add(name + " " + date);
            if (broken.contains(name)) {
                throw new java.io.IOException(name + ": the rides database did not answer");
            }
        };
    }

    /** An action that throws on its first 'failures' calls, then succeeds. */
    TaskAction flaky(String name, int failures) {
        int[] left = {failures};
        return date -> {
            calls.add(name + " " + date);
            if (left[0] > 0) {
                left[0]--;
                throw new IllegalStateException(name + ": timed out");
            }
        };
    }

    void breakTask(String name) {
        broken.add(name);
    }

    void fix(String name) {
        broken.remove(name);
    }

    /** Only the calls for one date. */
    List<String> callsOn(LocalDate date) {
        return calls.stream().filter(c -> c.endsWith(" " + date)).toList();
    }
}
