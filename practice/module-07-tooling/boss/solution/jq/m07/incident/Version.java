package jq.m07.incident;

/** A release version such as 1.10.0: major, minor and patch numbers. */
public record Version(int major, int minor, int patch) implements Comparable<Version> {

    public Version {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("version numbers cannot be negative: " + major + "." + minor + "." + patch);
        }
    }

    /** Reads "1.10.0". A leading "v", as in the git tag "v1.10.0", is allowed. */
    public static Version parse(String text) {
        String plain = text.strip();
        if (plain.startsWith("v")) {
            plain = plain.substring(1);
        }
        String[] parts = plain.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("expected major.minor.patch, got: " + text);
        }
        try {
            return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("expected whole numbers, got: " + text, e);
        }
    }

    @Override
    public int compareTo(Version other) {
        if (major != other.major) {
            return Integer.compare(major, other.major);
        }
        if (minor != other.minor) {
            return Integer.compare(minor, other.minor);
        }
        return Integer.compare(patch, other.patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
