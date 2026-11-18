package jq.m11.incident;

/**
 * What happened during one shutdown.
 *
 * @param inFlightAtStart requests that were running when shutdown began
 * @param cutOff          requests still running when the server stopped
 */
public record ShutdownReport(int inFlightAtStart, int cutOff) {

    /** True when every request that was running got to finish. */
    public boolean clean() {
        return cutOff == 0;
    }
}
