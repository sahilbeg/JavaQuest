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
        Map<?, ?> workflow = Workflows.load(yamlText);
        Map<?, ?> jobs = (Map<?, ?>) workflow.get("jobs");
        Set<String> triggers = Workflows.triggers(workflow);
        List<Finding> findings = new ArrayList<>();

        if ("write-all".equals(workflow.get("permissions"))) {
            findings.add(new Finding("BROAD_PERMISSIONS", "workflow",
                    "permissions: write-all gives the GITHUB_TOKEN every write scope; list only the scopes this needs"));
        } else if (!workflow.containsKey("permissions") && !everyJobSetsPermissions(jobs)) {
            findings.add(new Finding("NO_PERMISSIONS", "workflow",
                    "no top-level permissions: the GITHUB_TOKEN gets the repository default, which may allow writes"));
        }

        for (Map.Entry<?, ?> entry : jobs.entrySet()) {
            String where = "jobs." + entry.getKey();
            if (!(entry.getValue() instanceof Map<?, ?> job)) {
                throw new InvalidWorkflowException(where + " must be a mapping");
            }
            if ("write-all".equals(job.get("permissions"))) {
                findings.add(new Finding("BROAD_PERMISSIONS", where,
                        "permissions: write-all gives this job's GITHUB_TOKEN every write scope"));
            }
            if (job.get("uses") instanceof String reusable) {
                checkPin(reusable, where, findings);
            } else if (!job.containsKey("timeout-minutes")) {
                findings.add(new Finding("NO_TIMEOUT", where,
                        "no timeout-minutes: a stuck job runs for 6 hours (360 minutes) before GitHub stops it"));
            }
            if (job.get("steps") instanceof List<?> steps) {
                for (int i = 0; i < steps.size(); i++) {
                    if (steps.get(i) instanceof Map<?, ?> step) {
                        checkStep(step, where + ".steps[" + i + "]", triggers, findings);
                    }
                }
            }
        }
        return findings;
    }

    private static boolean everyJobSetsPermissions(Map<?, ?> jobs) {
        for (Object job : jobs.values()) {
            if (!(job instanceof Map<?, ?> map) || !map.containsKey("permissions")) {
                return false;
            }
        }
        return true;
    }

    private static void checkStep(Map<?, ?> step, String where, Set<String> triggers, List<Finding> findings) {
        if (step.get("uses") instanceof String uses) {
            checkPin(uses, where, findings);
            if (triggers.contains("pull_request_target") && uses.startsWith("actions/checkout@")
                    && step.get("with") instanceof Map<?, ?> with && with.get("ref") instanceof String ref
                    && PR_CODE_REFS.stream().anyMatch(ref::contains)) {
                findings.add(new Finding("PWN_REQUEST", where,
                        "pull_request_target runs with this repository's secrets and token, and this checks out the pull request's code: " + ref));
            }
        }
        if (step.get("run") instanceof String script) {
            for (String line : script.split("\n")) {
                if (SECRET.matcher(line).find() && (line.contains("echo") || line.contains("printf"))) {
                    findings.add(new Finding("SECRET_ECHOED", where,
                            "this run script prints a secret; pass it in through env: and never print it"));
                    break;
                }
            }
        }
    }

    private static void checkPin(String uses, String where, List<Finding> findings) {
        if (uses.startsWith("./") || uses.startsWith("docker://")) {
            return;
        }
        String owner = uses.substring(0, Math.max(0, uses.indexOf('/')));
        if (owner.equals("actions") || owner.equals("github")) {
            return;
        }
        int at = uses.lastIndexOf('@');
        if (at < 0 || !FULL_SHA.matcher(uses.substring(at + 1)).matches()) {
            findings.add(new Finding("UNPINNED_ACTION", where,
                    uses + " is not pinned to a full 40-character commit SHA, so its owner can move it to other code"));
        }
    }
}
