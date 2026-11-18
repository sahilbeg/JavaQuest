package jq.m11.dockerfiles;

import java.util.Set;

/**
 * One build step for the layer cache simulation.
 *
 * @param instruction the instruction text, such as "COPY pom.xml ." or "RUN mvn -q package"
 * @param reads       the build-context paths this step reads (empty for RUN): a file such as "pom.xml",
 *                    a folder such as "src" (everything under it), or "." (the whole context)
 * @param seconds     how long this step takes when it runs instead of coming from the cache
 */
public record Step(String instruction, Set<String> reads, long seconds) {

    public Step {
        reads = Set.copyOf(reads);
    }

    public static Step run(String instruction, long seconds) {
        return new Step(instruction, Set.of(), seconds);
    }

    public static Step copy(String instruction, Set<String> reads, long seconds) {
        return new Step(instruction, reads, seconds);
    }
}
