package jq.m11.incident;

import java.util.regex.Pattern;

/**
 * Spoke's rules for which image a deploy may use. CI calls releaseTag to name each image it pushes,
 * and the deploy tool calls requireImmutable before it changes the Deployment.
 *
 * This is the code that ran on Friday.
 */
public final class ImagePolicy {

    private ImagePolicy() {
    }

    /** The tag CI gives an image: the release version, a dash, and the first 12 characters of the commit SHA. */
    public static String releaseTag(String version, String commitSha) {
        // TODO 1 (bronze): return version + "-" + the first 12 characters of commitSha, for example
        // "2.5.1-3f9c2b7a1d4e". Throw IllegalArgumentException unless version is three numbers like 2.5.1
        // and commitSha is exactly 40 characters, each 0-9 or a-f.
        return "latest";
    }

    /** Returns the image unchanged when it names one exact build; throws MutableImageException otherwise. */
    public static String requireImmutable(String image) {
        // TODO 2 (bronze): accept only an image pinned by a sha256 digest, or one whose tag is a release tag
        // from releaseTag. Throw new MutableImageException(image, reason) for latest, no tag, main, stable,
        // a bare version, a bad digest, null or blank. A colon before the last slash is a registry port, not a tag.
        return image;
    }
}
