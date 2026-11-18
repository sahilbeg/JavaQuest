package jq.m11.dockerfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A small model of Docker's build cache for one stage. A step is reused from the cache only when every step
 * before it was reused, its instruction text is the same as last time, and none of the files it reads changed.
 */
public final class LayerCache {

    private final List<String> ignored = new ArrayList<>();

    /** @param dockerignore the lines of .dockerignore: plain paths, no wildcards; blank lines and # comments are skipped */
    public LayerCache(List<String> dockerignore) {
        for (String line : dockerignore) {
            String path = line.strip();
            if (path.isEmpty() || path.startsWith("#")) {
                continue;
            }
            ignored.add(trimSlashes(path));
        }
    }

    /** True when {@code file} is {@code path} itself or lies inside it. The path "." covers every file. */
    static boolean covers(String path, String file) {
        String p = trimSlashes(path);
        return p.equals(".") || p.isEmpty() || file.equals(p) || file.startsWith(p + "/");
    }

    /** True when .dockerignore leaves this file out of the build context. */
    public boolean isIgnored(String file) {
        return ignored.stream().anyMatch(path -> covers(path, file));
    }

    /** True when this step reads at least one changed file that is not ignored. */
    public boolean readsChange(Step step, Set<String> changedFiles) {
        for (String file : changedFiles) {
            if (isIgnored(file)) {
                continue;
            }
            for (String read : step.reads()) {
                if (covers(read, file)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The 1-based numbers of the steps that run again; an empty list when everything comes from the cache. */
    public List<Integer> rebuilds(List<Step> previous, List<Step> current, Set<String> changedFiles) {
        List<Integer> result = new ArrayList<>();
        boolean invalidated = false;
        for (int i = 0; i < current.size(); i++) {
            Step step = current.get(i);
            if (!invalidated) {
                boolean sameInstruction = i < previous.size()
                        && previous.get(i).instruction().equals(step.instruction());
                invalidated = !sameInstruction || readsChange(step, changedFiles);
            }
            if (invalidated) {
                result.add(i + 1);
            }
        }
        return result;
    }

    /** The total seconds of the steps that run again. */
    public long rebuildSeconds(List<Step> previous, List<Step> current, Set<String> changedFiles) {
        long total = 0;
        for (int number : rebuilds(previous, current, changedFiles)) {
            total += current.get(number - 1).seconds();
        }
        return total;
    }

    private static String trimSlashes(String path) {
        String p = path.startsWith("./") ? path.substring(2) : path;
        while (p.endsWith("/") && p.length() > 1) {
            p = p.substring(0, p.length() - 1);
        }
        while (p.startsWith("/") && p.length() > 1) {
            p = p.substring(1);
        }
        return p;
    }
}
