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
        // TODO 14: for each line, strip it; skip blank lines and lines starting with #;
        //          add the rest to ignored, cleaned with trimSlashes (so "target/" becomes "target").
    }

    /** True when {@code file} is {@code path} itself or lies inside it. The path "." covers every file. */
    static boolean covers(String path, String file) {
        String p = trimSlashes(path);
        // TODO 15: true when p is "." or empty, when file equals p, or when file starts with p + "/".
        //          ("src" covers "src/A.java" but not "srcold/A.java".)
        return false;
    }

    /** True when .dockerignore leaves this file out of the build context. */
    public boolean isIgnored(String file) {
        // TODO 16: true when any ignored path covers the file.
        return false;
    }

    /** True when this step reads at least one changed file that is not ignored. */
    public boolean readsChange(Step step, Set<String> changedFiles) {
        // TODO 17: skip ignored files; return true when any of step.reads() covers a changed file.
        return false;
    }

    /** The 1-based numbers of the steps that run again; an empty list when everything comes from the cache. */
    public List<Integer> rebuilds(List<Step> previous, List<Step> current, Set<String> changedFiles) {
        List<Integer> result = new ArrayList<>();
        // TODO 18: walk current in order. The first step that is invalidated is one where
        //          - there is no previous step at the same position, or its instruction text differs, or
        //          - readsChange(step, changedFiles) is true.
        //          That step and EVERY step after it rebuild. Add their 1-based numbers (i + 1).
        return result;
    }

    /** The total seconds of the steps that run again. */
    public long rebuildSeconds(List<Step> previous, List<Step> current, Set<String> changedFiles) {
        // TODO 19: add up current.get(number - 1).seconds() for every number in rebuilds(...).
        return 0;
    }

    /** Given: removes a leading "./" or "/" and trailing "/", so "./target/" becomes "target". */
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
