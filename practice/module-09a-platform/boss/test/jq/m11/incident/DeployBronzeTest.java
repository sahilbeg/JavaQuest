package jq.m11.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeployBronzeTest {

    private static final String SHA = "3f9c2b7a1d4e5f60718293a4b5c6d7e8f9012345";
    private static final String DIGEST = "sha256:" + "4b1d".repeat(16);
    private static final String REPO = "ghcr.io/spoke/spoke-api";

    private static void assertRefused(String image, String why) {
        MutableImageException error = assertThrows(MutableImageException.class,
                () -> ImagePolicy.requireImmutable(image),
                "requireImmutable must refuse " + image + " with MutableImageException: " + why);
        assertTrue(error.getMessage() != null && error.getMessage().contains(String.valueOf(image)),
                "the exception message should name the refused image, so the deploy log shows it");
    }

    private static void assertAccepted(String image, String why) {
        assertEquals(image, ImagePolicy.requireImmutable(image),
                "requireImmutable should return " + image + " unchanged: " + why);
    }

    @Test
    @DisplayName("Bronze: a release tag is the version, a dash, and the first 12 characters of the commit SHA")
    void releaseTagFormat() {
        assertEquals("2.5.1-3f9c2b7a1d4e", ImagePolicy.releaseTag("2.5.1", SHA),
                "version 2.5.1 at commit 3f9c2b7a1d4e... should give the tag 2.5.1-3f9c2b7a1d4e");
        assertEquals("10.0.12-0123456789ab", ImagePolicy.releaseTag("10.0.12", "0123456789ab" + "c".repeat(28)),
                "every part of the version can have more than one digit");
    }

    @Test
    @DisplayName("Bronze: releaseTag refuses a version that is not three numbers, or a commit SHA that is not 40 hex characters")
    void releaseTagRefusesBadInput() {
        for (String version : new String[] {"v2.5.1", "2.5", "latest", "2.5.1-rc1", ""}) {
            assertThrows(IllegalArgumentException.class, () -> ImagePolicy.releaseTag(version, SHA),
                    "\"" + version + "\" is not three numbers like 2.5.1, so releaseTag should throw IllegalArgumentException");
        }
        for (String sha : new String[] {"3f9c2b7a1d4e", SHA.toUpperCase(), SHA + "0", SHA.replace('f', 'g'), ""}) {
            assertThrows(IllegalArgumentException.class, () -> ImagePolicy.releaseTag("2.5.1", sha),
                    "\"" + sha + "\" is not the full 40-character lowercase commit SHA, so releaseTag should throw IllegalArgumentException");
        }
        assertThrows(IllegalArgumentException.class, () -> ImagePolicy.releaseTag(null, SHA),
                "a missing version should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> ImagePolicy.releaseTag("2.5.1", null),
                "a missing commit SHA should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Bronze: an image with a release tag, or pinned by a sha256 digest, passes unchanged")
    void immutableReferencesPass() {
        assertAccepted(REPO + ":2.5.1-3f9c2b7a1d4e", "its tag is a release tag");
        assertAccepted(REPO + "@" + DIGEST, "a digest names exactly one image");
        assertAccepted(REPO + ":2.5.1-3f9c2b7a1d4e@" + DIGEST, "with a digest, the digest decides the image");
        assertAccepted("spoke-api:0.1.0-0123456789ab", "an image without a registry can still have a release tag");
    }

    @Test
    @DisplayName("Bronze: latest, or no tag at all, is refused")
    void latestIsRefused() {
        assertRefused(REPO + ":latest", "latest is whatever someone pushed last, and nodes cache it");
        assertRefused(REPO, "with no tag, Docker and Kubernetes use latest");
    }

    @Test
    @DisplayName("Bronze: tags that can be moved are refused: main, stable, a bare version, a bare commit SHA")
    void movableTagsAreRefused() {
        for (String tag : List.of("main", "stable", "2.5.1", "3f9c2b7a1d4e", "2.5.1-3F9C2B7A1D4E", "2.5.1-3f9c2b7a1d4e5f6", "v2.5.1-3f9c2b7a1d4e")) {
            assertRefused(REPO + ":" + tag, "only <version>-<12 lowercase hex characters of the commit SHA> is a release tag");
        }
    }

    @Test
    @DisplayName("Bronze: the colon in a registry port is not a tag")
    void registryPortIsNotATag() {
        assertRefused("registry.spoke.local:5000/spoke/spoke-api",
                "5000/spoke/spoke-api is the port and path, not a tag; this image has no tag at all");
        assertAccepted("registry.spoke.local:5000/spoke/spoke-api:2.5.1-3f9c2b7a1d4e",
                "the tag is after the last slash, and it is a release tag");
    }

    @Test
    @DisplayName("Bronze: a digest must be sha256 and 64 lowercase hex characters")
    void badDigestsAreRefused() {
        assertRefused(REPO + "@sha256:4b1d", "a short digest is not a digest");
        assertRefused(REPO + "@" + DIGEST.toUpperCase().replace("SHA256", "sha256"), "the hex must be lowercase");
        assertRefused(REPO + "@md5:" + "4b1d".repeat(8), "Spoke pins images with sha256 digests only");
        assertRefused(REPO + ":latest@sha256:", "an empty digest pins nothing");
    }

    @Test
    @DisplayName("Bronze: a missing or blank image is refused")
    void blankIsRefused() {
        assertThrows(MutableImageException.class, () -> ImagePolicy.requireImmutable(null),
                "a deploy with no image should throw MutableImageException");
        assertThrows(MutableImageException.class, () -> ImagePolicy.requireImmutable("  "),
                "a blank image should throw MutableImageException");
    }
}
