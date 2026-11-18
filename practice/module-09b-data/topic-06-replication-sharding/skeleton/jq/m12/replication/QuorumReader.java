package jq.m12.replication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Reads from R of N replicas, keeps the newest version, and lists the replicas that need read repair. */
public final class QuorumReader {

    private final int n;
    private final int r;

    public QuorumReader(int n, int r) {
        // TODO 17: throw IllegalArgumentException unless 1 <= r <= n
        this.n = n;
        this.r = r;
    }

    /** true when every read quorum of R overlaps every write quorum of W among N replicas: W + R > N. */
    public static boolean overlaps(int n, int w, int r) {
        // TODO 18: IllegalArgumentException unless 1 <= w <= n and 1 <= r <= n
        return false;
    }

    /** The newest answer wins; older answers need repair. Fewer than R answers: QuorumNotReachedException. */
    public ReadResult read(List<ReplicaAnswer> answers) {
        List<String> needRepair = new ArrayList<>();
        // TODO 19: fewer than r answers -> throw new QuorumNotReachedException("needed " + r + " answers, got " + size)
        // TODO 20: find the highest version. Every answer with a lower version goes into needRepair, in answer order.
        //          An answer with the highest version but a different value (Objects.equals) is a conflict:
        //          throw IllegalStateException.
        return new ReadResult(null, 0, needRepair);
    }
}
