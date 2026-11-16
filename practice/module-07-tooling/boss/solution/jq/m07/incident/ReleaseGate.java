package jq.m07.incident;

/** The first pipeline stage: a release may only ship when it is newer than the version that is live. */
public final class ReleaseGate {

    private ReleaseGate() {
    }

    public static boolean canShip(String liveVersion, String candidateVersion) {
        return Version.parse(candidateVersion).compareTo(Version.parse(liveVersion)) > 0;
    }
}
