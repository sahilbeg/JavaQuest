package jq.m11.dockerfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LayerCacheTest {

    /** Dependencies first: the slow download sits above the code. */
    static final List<Step> GOOD_ORDER = List.of(
            Step.run("FROM maven:3.9.11-eclipse-temurin-21 AS build", 0),
            Step.run("WORKDIR /src", 0),
            Step.copy("COPY pom.xml .", Set.of("pom.xml"), 1),
            Step.run("RUN mvn -q -B dependency:go-offline", 420),
            Step.copy("COPY src ./src", Set.of("src"), 1),
            Step.run("RUN mvn -q -B package -DskipTests", 60));

    /** Everything copied first: any change busts the cache for the download. */
    static final List<Step> BAD_ORDER = List.of(
            Step.run("FROM maven:3.9.11-eclipse-temurin-21 AS build", 0),
            Step.run("WORKDIR /src", 0),
            Step.copy("COPY . .", Set.of("."), 2),
            Step.run("RUN mvn -q -B dependency:go-offline", 420),
            Step.run("RUN mvn -q -B package -DskipTests", 60));

    static final String CHANGED_CLASS = "src/main/java/spoke/api/FareService.java";

    @Test
    @DisplayName("With dependencies first, a code change rebuilds only the last two steps")
    void goodOrderKeepsTheDownloadCached() {
        LayerCache cache = new LayerCache(List.of());
        assertEquals(List.of(5, 6), cache.rebuilds(GOOD_ORDER, GOOD_ORDER, Set.of(CHANGED_CLASS)),
                "FareService.java is under src, so COPY src (step 5) is the first step to change. It and every step after it rebuild");
        assertEquals(61, cache.rebuildSeconds(GOOD_ORDER, GOOD_ORDER, Set.of(CHANGED_CLASS)),
                "only steps 5 and 6 run again: 1 + 60 seconds. The 420-second download stays cached");
        assertEquals(List.of(3, 4, 5, 6), cache.rebuilds(GOOD_ORDER, GOOD_ORDER, Set.of("pom.xml")),
                "a pom.xml change busts COPY pom.xml (step 3), so the download runs again, and so does everything after it");
    }

    @Test
    @DisplayName("With COPY . . first, the same one-line code change runs the slow download again")
    void badOrderBustsTheCache() {
        LayerCache cache = new LayerCache(List.of());
        assertEquals(List.of(3, 4, 5), cache.rebuilds(BAD_ORDER, BAD_ORDER, Set.of(CHANGED_CLASS)),
                "COPY . . reads every file, so any change busts step 3, and steps 4 and 5 rebuild after it");
        assertEquals(482, cache.rebuildSeconds(BAD_ORDER, BAD_ORDER, Set.of(CHANGED_CLASS)),
                "2 + 420 + 60 = 482 seconds: about eight minutes for a one-line change");
    }

    @Test
    @DisplayName("Nothing changed means everything is cached; a first build, with no previous steps, runs everything")
    void noChangesAndFirstBuild() {
        LayerCache cache = new LayerCache(List.of());
        assertEquals(List.of(), cache.rebuilds(GOOD_ORDER, GOOD_ORDER, Set.of()),
                "same instructions and no changed files: every step comes from the cache");
        assertEquals(0, cache.rebuildSeconds(GOOD_ORDER, GOOD_ORDER, Set.of()), "a fully cached build takes 0 seconds here");
        assertEquals(List.of(1, 2, 3, 4, 5, 6), cache.rebuilds(List.of(), GOOD_ORDER, Set.of()),
                "with no previous build there is no cache, so every step runs");
        assertEquals(482, new LayerCache(List.of()).rebuildSeconds(List.of(), BAD_ORDER, Set.of()),
                "a first build of the bad order takes 0 + 0 + 2 + 420 + 60 seconds");
    }

    @Test
    @DisplayName("Editing an instruction's text busts the cache from that step on, even with no changed files")
    void editedInstructionBustsTheCache() {
        List<Step> edited = List.of(
                GOOD_ORDER.get(0), GOOD_ORDER.get(1), GOOD_ORDER.get(2), GOOD_ORDER.get(3), GOOD_ORDER.get(4),
                Step.run("RUN mvn -q -B package -DskipTests -Pprod", 60));
        LayerCache cache = new LayerCache(List.of());
        assertEquals(List.of(6), cache.rebuilds(GOOD_ORDER, edited, Set.of()),
                "step 6's text changed, so it rebuilds. Steps 1 to 5 are identical and read nothing new");

        List<Step> longer = new ArrayList<>(GOOD_ORDER);
        longer.add(Step.run("RUN java -Djarmode=tools -jar target/spoke-api.jar extract --layers --destination /extracted", 3));
        assertEquals(List.of(7), cache.rebuilds(GOOD_ORDER, longer, Set.of()),
                "a step added at the end was never built, so it runs; the six steps before it stay cached");
    }

    @Test
    @DisplayName(".dockerignore keeps changes to ignored files from busting COPY . .")
    void dockerignoreProtectsTheCache() {
        LayerCache cache = new LayerCache(List.of("# build output and history", ".git", "target/", "", "README.md"));
        assertTrue(cache.isIgnored(".git/HEAD"), ".git covers every file inside the .git folder");
        assertTrue(cache.isIgnored("target/spoke-api.jar"), "target/ with a slash at the end still means the target folder");
        assertFalse(cache.isIgnored("targets.txt"), "target is a folder name, not a prefix of any name: targets.txt is not ignored");
        assertTrue(cache.isIgnored("README.md"), "README.md is listed, so the README at the top of the project is ignored");
        assertFalse(cache.isIgnored("docs/README.md"), "paths start at the top of the build context: README.md does not cover docs/README.md");

        assertEquals(List.of(), cache.rebuilds(BAD_ORDER, BAD_ORDER, Set.of(".git/HEAD", "target/spoke-api.jar", "README.md")),
                "every changed file is ignored, so it never reaches the build context and COPY . . stays cached");
        assertEquals(List.of(3, 4, 5), cache.rebuilds(BAD_ORDER, BAD_ORDER, Set.of("README.md", CHANGED_CLASS)),
                "README.md is ignored, but the Java file is not, so COPY . . still rebuilds");
        assertEquals(List.of(), new LayerCache(List.of()).rebuilds(GOOD_ORDER, GOOD_ORDER, Set.of("srcold/Notes.java")),
                "COPY src reads the src folder only: srcold/Notes.java is not inside it");
    }
}
