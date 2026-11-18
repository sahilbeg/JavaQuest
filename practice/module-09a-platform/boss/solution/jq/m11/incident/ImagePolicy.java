package jq.m11.incident;

import java.util.regex.Pattern;

/**
 * Spoke's rules for which image a deploy may use. CI calls releaseTag to name each image it pushes,
 * and the deploy tool calls requireImmutable before it changes the Deployment.
 */
public final class ImagePolicy {

    private static final Pattern VERSION = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    private static final Pattern COMMIT_SHA = Pattern.compile("[0-9a-f]{40}");
    private static final Pattern RELEASE_TAG = Pattern.compile("\\d+\\.\\d+\\.\\d+-[0-9a-f]{12}");
    private static final Pattern DIGEST = Pattern.compile("sha256:[0-9a-f]{64}");

    private ImagePolicy() {
    }

    /** The tag CI gives an image: the release version, a dash, and the first 12 characters of the commit SHA. */
    public static String releaseTag(String version, String commitSha) {
        if (version == null || !VERSION.matcher(version).matches()) {
            throw new IllegalArgumentException("version must be three numbers like 2.5.1, but was " + version);
        }
        if (commitSha == null || !COMMIT_SHA.matcher(commitSha).matches()) {
            throw new IllegalArgumentException(
                    "commitSha must be the full 40-character lowercase commit SHA, but was " + commitSha);
        }
        return version + "-" + commitSha.substring(0, 12);
    }

    /** Returns the image unchanged when it names one exact build; throws MutableImageException otherwise. */
    public static String requireImmutable(String image) {
        if (image == null || image.isBlank()) {
            throw new MutableImageException(String.valueOf(image), "no image given");
        }
        int at = image.indexOf('@');
        if (at >= 0) {
            if (at == 0) {
                throw new MutableImageException(image, "no repository before the digest");
            }
            if (!DIGEST.matcher(image.substring(at + 1)).matches()) {
                throw new MutableImageException(image, "a digest is sha256: and 64 lowercase hex characters");
            }
            return image;
        }
        // The tag is after the last colon in the last part of the name. A colon before the last slash
        // belongs to the registry's port, as in registry.spoke.local:5000/spoke/spoke-api.
        int lastSlash = image.lastIndexOf('/');
        int colon = image.indexOf(':', lastSlash + 1);
        if (colon < 0) {
            throw new MutableImageException(image, "no tag, so Docker would use latest");
        }
        if (colon == 0) {
            throw new MutableImageException(image, "no repository before the tag");
        }
        String tag = image.substring(colon + 1);
        if (!RELEASE_TAG.matcher(tag).matches()) {
            throw new MutableImageException(image,
                    "the tag " + tag + " can be moved; use <version>-<12-character commit SHA> or a sha256 digest");
        }
        return image;
    }
}
