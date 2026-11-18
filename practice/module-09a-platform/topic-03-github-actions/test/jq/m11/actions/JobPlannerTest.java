package jq.m11.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JobPlannerTest {

    private final JobPlanner planner = new JobPlanner();

    @Test
    @DisplayName("The release pipeline runs in waves: each job starts once everything it needs has finished")
    void releasePipelineWaves() {
        String yaml = """
                on: push
                jobs:
                  build:
                    runs-on: ubuntu-latest
                  unit-tests:
                    needs: build
                    runs-on: ubuntu-latest
                  image:
                    needs: [build]
                    runs-on: ubuntu-latest
                  scan:
                    needs: image
                    runs-on: ubuntu-latest
                  deploy-staging:
                    needs: [unit-tests, scan]
                    runs-on: ubuntu-latest
                  deploy-prod:
                    needs: deploy-staging
                    runs-on: ubuntu-latest
                """;
        List<List<String>> waves = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> planner.waves(yaml),
                "waves() should finish quickly");
        assertEquals(List.of(
                        List.of("build"),
                        List.of("unit-tests", "image"),
                        List.of("scan"),
                        List.of("deploy-staging"),
                        List.of("deploy-prod")),
                waves,
                "needs: can be one name or a list. deploy-staging must wait for both unit-tests and scan, so it is in wave 4");
    }

    @Test
    @DisplayName("Jobs with no needs all start together in the first wave, in file order, not sorted by name")
    void independentJobsShareTheFirstWave() {
        String yaml = """
                on: pull_request
                jobs:
                  lint:
                    runs-on: ubuntu-latest
                  unit-tests:
                    runs-on: ubuntu-latest
                  docs:
                    runs-on: ubuntu-latest
                  report:
                    needs: [lint, docs, unit-tests]
                    runs-on: ubuntu-latest
                """;
        assertEquals(List.of(List.of("lint", "unit-tests", "docs"), List.of("report")),
                assertTimeoutPreemptively(Duration.ofSeconds(2), () -> planner.waves(yaml), "waves() should finish quickly"),
                "lint, unit-tests and docs need nothing, so they share wave 1 in the order the file lists them");
    }

    @Test
    @DisplayName("A job that needs a job that does not exist is rejected, and the message names both")
    void unknownNeedIsRejected() {
        String yaml = """
                on: push
                jobs:
                  build:
                    runs-on: ubuntu-latest
                  deploy:
                    needs: [build, tests]
                    runs-on: ubuntu-latest
                """;
        InvalidWorkflowException e = assertThrows(InvalidWorkflowException.class, () -> planner.waves(yaml),
                "deploy needs tests, but there is no job called tests: throw InvalidWorkflowException");
        assertTrue(e.getMessage() != null && e.getMessage().contains("deploy") && e.getMessage().contains("tests"),
                "the message should name the job (deploy) and the missing need (tests), but it was: " + e.getMessage());
    }

    @Test
    @DisplayName("Needs that go round in a circle are rejected as a cycle instead of looping forever")
    void cyclesAreRejected() {
        String yaml = """
                on: push
                jobs:
                  build:
                    runs-on: ubuntu-latest
                  image:
                    needs: [build, scan]
                    runs-on: ubuntu-latest
                  scan:
                    needs: image
                    runs-on: ubuntu-latest
                """;
        InvalidWorkflowException e = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> assertThrows(InvalidWorkflowException.class, () -> planner.waves(yaml),
                        "image needs scan and scan needs image: throw InvalidWorkflowException"),
                "a cycle must be detected, not waited on forever");
        String message = e.getMessage() == null ? "" : e.getMessage();
        assertTrue(message.toLowerCase().contains("cycle") && message.contains("image") && message.contains("scan"),
                "the message should say cycle and name image and scan, but it was: " + message);

        String selfNeed = "on: push\njobs:\n  build:\n    needs: build\n    runs-on: ubuntu-latest\n";
        assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> assertThrows(InvalidWorkflowException.class, () -> planner.waves(selfNeed),
                        "a job that needs itself can never start: that is a cycle too"),
                "a job that needs itself must be detected, not waited on forever");
    }

    @Test
    @DisplayName("A matrix makes one job per combination, and exclude removes every combination it matches")
    void matrixCountsCombinations() {
        String yaml = """
                on: push
                jobs:
                  lint:
                    runs-on: ubuntu-latest
                  build:
                    runs-on: ${{ matrix.os }}
                    strategy:
                      matrix:
                        os: [ubuntu-latest, windows-latest]
                        java: [17, 21, 25]
                  compat:
                    runs-on: ${{ matrix.os }}
                    strategy:
                      matrix:
                        os: [ubuntu-latest, windows-latest, macos-latest]
                        java: [17, 21, 25]
                        exclude:
                          - os: windows-latest
                          - os: macos-latest
                            java: 17
                """;
        assertEquals(1, planner.jobCount(yaml, "lint"), "a job without a strategy.matrix is exactly 1 job");
        assertEquals(6, planner.jobCount(yaml, "build"), "2 operating systems x 3 Java versions = 6 jobs");
        assertEquals(5, planner.jobCount(yaml, "compat"),
                "3 x 3 = 9; exclude os: windows-latest removes all 3 Windows jobs, and macos-latest with java 17 removes 1 more: 5");
    }

    @Test
    @DisplayName("An include adds a job only when no combination agrees with it; one that fits merges in and adds nothing")
    void includeAddsOnlyWhenNothingFits() {
        String yaml = """
                on: push
                jobs:
                  build:
                    runs-on: ${{ matrix.os }}
                    strategy:
                      matrix:
                        os: [ubuntu-latest, windows-latest]
                        java: [21, 25]
                        exclude:
                          - os: windows-latest
                            java: 21
                        include:
                          - os: ubuntu-latest
                            experimental: true
                          - os: ubuntu-latest
                            java: 17
                          - os: windows-latest
                            java: 21
                  smoke:
                    runs-on: ${{ matrix.os }}
                    strategy:
                      matrix:
                        include:
                          - os: ubuntu-latest
                          - os: macos-latest
                """;
        assertEquals(5, planner.jobCount(yaml, "build"),
                "4 combinations, minus 1 excluded = 3. {ubuntu, experimental} fits the ubuntu jobs: +0. {ubuntu, java 17} fits none: +1. "
                        + "{windows, java 21} was excluded, so nothing fits it now and it comes back: +1. Total 5");
        assertEquals(2, planner.jobCount(yaml, "smoke"),
                "a matrix with only include: entries makes one job per entry: 2");
        assertThrows(InvalidWorkflowException.class, () -> planner.jobCount(yaml, "deploy"),
                "there is no job called deploy: throw InvalidWorkflowException");
    }
}
