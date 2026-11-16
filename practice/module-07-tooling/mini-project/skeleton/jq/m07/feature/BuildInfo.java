package jq.m07.feature;

/** What the build stamped into the jar's manifest. */
public final class BuildInfo {

    private BuildInfo() {
    }

    /** The Implementation-Version from the jar's manifest, or "dev" when the code is not running from a jar. */
    public static String version() {
        // TODO 4: ask BuildInfo.class.getPackage() for its implementation version. It is null outside a jar.
        return "";
    }
}
