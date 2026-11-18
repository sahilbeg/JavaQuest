package jq.m12.replication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Reads from R of N replicas, keeps the newest version, and lists the replicas that need read repair. */
public final class QuorumReader {

    private final int n;
    private final int r;

    public QuorumReader(int n, int r) {
        if (n < 1 || r < 1 || r > n) {
            throw new IllegalArgumentException("need 1 <= R <= N, got N=" + n + ", R=" + r);
        }
        this.n = n;
        this.r = r;
    }

    public static boolean overlaps(int n, int w, int r) {
        if (n < 1 || w < 1 || w > n || r < 1 || r > n) {
            throw new IllegalArgumentException("need 1 <= W <= N and 1 <= R <= N, got N=" + n + ", W=" + w + ", R=" + r);
        }
        return w + r > n;
    }

    public ReadResult read(List<ReplicaAnswer> answers) {
        if (answers.size() < r) {
            throw new QuorumNotReachedException("needed " + r + " answers, got " + answers.size());
        }
        ReplicaAnswer newest = answers.get(0);
        for (ReplicaAnswer answer : answers) {
            if (answer.version() > newest.version()) {
                newest = answer;
            }
        }
        List<String> needRepair = new ArrayList<>();
        for (ReplicaAnswer answer : answers) {
            if (answer.version() < newest.version()) {
                needRepair.add(answer.replica());
            } else if (!Objects.equals(answer.value(), newest.value())) {
                throw new IllegalStateException("conflict at version " + newest.version() + ": "
                        + newest.replica() + " and " + answer.replica() + " hold different values");
            }
        }
        return new ReadResult(newest.value(), newest.version(), List.copyOf(needRepair));
    }
}
