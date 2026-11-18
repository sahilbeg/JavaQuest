package jq.m11.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Checks a workflow file against Spoke's CI security rules. */
public final class WorkflowAudit {

    private static final Pattern FULL_SHA = Pattern.compile("[0-9a-f]{40}");
    private static final Pattern SECRET = Pattern.compile("\\$\\{\\{\\s*secrets\\.");
    private static final List<String> PR_CODE_REFS =
            List.of("github.event.pull_request.head", "github.head_ref", "refs/pull/");

    /** Every finding, in the order the README describes. An empty list means the workflow passes. */
    public List<Finding> audit(String yamlText) {
        List<Finding> findings = new ArrayList<>();
        // Start with: Map<?, ?> workflow = Workflows.load(yamlText);
        //             Map<?, ?> jobs = (Map<?, ?>) workflow.get("jobs");
        //             Set<String> triggers = Workflows.triggers(workflow);
        // TODO 4: workflow level. permissions: write-all gives BROAD_PERMISSIONS at "workflow". No permissions key at all
        //         gives NO_PERMISSIONS at "workflow", unless every job has its own permissions key.
        // TODO 5: for each job in file order, where = "jobs." + name: write-all gives BROAD_PERMISSIONS. A job with a uses:
        //         (a reusable workflow) goes through checkPin; any other job without timeout-minutes gives NO_TIMEOUT.
        //         Then call checkStep for each step, with where + ".steps[" + i + "]" (i counts from 0).
        return findings;
    }

    private static void checkStep(Map<?, ?> step, String where, Set<String> triggers, List<Finding> findings) {
        // TODO 6: a step with uses: goes through checkPin first.
        // TODO 7: PWN_REQUEST when the triggers contain pull_request_target, uses starts with "actions/checkout@",
        //         and with.ref contains one of PR_CODE_REFS.
        // TODO 8: SECRET_ECHOED (at most once per step) when one line of run: matches SECRET and contains echo or printf.
    }

    private static void checkPin(String uses, String where, List<Finding> findings) {
        // TODO 9: skip uses: values starting with ./ or docker://, and owners actions and github (the part before the
        //         first /). Otherwise the text after the last @ must match FULL_SHA, or add UNPINNED_ACTION with a
        //         message that quotes the uses: value.
    }
}
