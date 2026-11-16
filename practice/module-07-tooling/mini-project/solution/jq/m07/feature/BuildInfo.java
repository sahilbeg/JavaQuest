package jq.m07.feature;

/** What the build stamped into the jar's manifest. */
public final class BuildInfo {

    private BuildInfo() {
    }

    /** The Implementation-Version from the jar's manifest, or "dev" when the code is not running from a jar. */
    public static String version() {
        String version = BuildInfo.class.getPackage().getImplementationVersion();
        return version == null ? "dev" : version;
    }
}
