package jq.m11.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Plans a workflow run: which jobs can run side by side, and how many jobs a matrix makes. */
public final class JobPlanner {

    /** The jobs in waves: every job in a wave has all its needs in earlier waves. File order inside a wave. */
    public List<List<String>> waves(String yamlText) {
        // Start with: Map<?, ?> jobs = (Map<?, ?>) Workflows.load(yamlText).get("jobs");
        // TODO 10: read each job's needs with needsOf. A need that is not a job name: throw InvalidWorkflowException
        //          naming the job and the missing need.
        // TODO 11: wave 1 is every job with no needs, in file order. Each later wave is every job whose needs are all
        //          in earlier waves, in file order. (Kahn's algorithm: count unfinished needs per job.)
        // TODO 12: if some jobs can never be planned, their needs form a cycle: throw InvalidWorkflowException
        //          with the word cycle and the names of the jobs that are stuck.
        return List.of();
    }

    private static List<String> needsOf(String job, Object value) {
        // TODO 13: null gives an empty list, a String gives one name, a List gives its names.
        //          Anything else: throw InvalidWorkflowException.
        return List.of();
    }

    /** How many jobs this job becomes: 1 without a matrix, otherwise its combinations after exclude and include. */
    public int jobCount(String yamlText, String jobName) {
        // TODO 14: no job called jobName: throw InvalidWorkflowException. No strategy.matrix: return 1.
        // TODO 15: every matrix key except include and exclude is an axis with a list of values. Build every
        //          combination (a Map from axis to value). A matrix with no axes makes one job per include entry.
        // TODO 16: remove every combination that matches an exclude entry (all of the entry's keys and values agree).
        // TODO 17: each include entry adds one job when no remaining combination agrees with it on all the axis
        //          keys it mentions; otherwise it merges in and adds nothing. Compare values with Objects.equals.
        return 0;
    }
}
