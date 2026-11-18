package jq.m11.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkflowAuditTest {

    private static final String SHA = "3f9c2a1b7e4d5c6a8b9f0e1d2c3b4a5968778695";

    private final WorkflowAudit audit = new WorkflowAudit();

    /** Each finding as "RULE @ where", and a check that every message is filled in. */
    private List<String> run(String yaml) {
        List<Finding> findings = audit.audit(yaml);
        List<String> summary = new ArrayList<>();
        for (Finding f : findings) {
            assertTrue(f.message() != null && !f.message().isBlank(),
                    "every finding needs a message for the person reading the pull request, but " + f.rule() + " at " + f.where() + " has none");
            summary.add(f.rule() + " @ " + f.where());
        }
        return summary;
    }

    @Test
    @DisplayName("A hardened workflow passes with no findings, and dropping its permissions block is caught")
    void hardenedWorkflowIsClean() {
        String yaml = """
                name: CI
                on:
                  pull_request:
                  push:
                    branches: [main]
                permissions:
                  contents: read
                concurrency:
                  group: ci-${{ github.ref }}
                  cancel-in-progress: true
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    steps:
                      - uses: actions/checkout@v5
                      - uses: actions/setup-java@v5
                        with:
                          distribution: temurin
                          java-version: '21'
                          cache: maven
                      - run: |
                          echo "Building ${{ github.sha }}"
                          ./mvnw -B verify
                      - uses: ./.github/actions/report
                      - uses: aquasecurity/trivy-action@%s
                      - env:
                          DEPLOY_KEY: ${{ secrets.DEPLOY_KEY }}
                        run: ./scripts/check-key.sh "$DEPLOY_KEY"
                """.formatted(SHA);
        assertEquals(List.of(), run(yaml),
                "permissions are set, the job has a timeout, the outside action is pinned to a full SHA, actions/* may use a tag, "
                        + "./ actions live in the repo, and the secret goes through env:. Nothing to report");
        assertEquals(List.of("NO_PERMISSIONS @ workflow"), run(yaml.replace("permissions:\n  contents: read\n", "")),
                "the same workflow without its permissions: block should give exactly one finding, NO_PERMISSIONS at workflow");
    }

    @Test
    @DisplayName("An outside action on a tag, a branch, a short SHA or no ref at all is UNPINNED_ACTION; the message names it")
    void unpinnedActionsAreFound() {
        String yaml = """
                on: push
                permissions:
                  contents: read
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    steps:
                      - uses: actions/checkout@v5
                      - uses: tj-actions/changed-files@v46
                      - uses: docker/build-push-action@master
                      - uses: aquasecurity/trivy-action@3f9c2a1
                      - uses: spoke/setup-tools
                      - uses: github/codeql-action/init@v3
                      - uses: docker://alpine:3.20
                      - uses: reviewdog/action-setup@%s
                """.formatted(SHA);
        assertEquals(List.of(
                        "UNPINNED_ACTION @ jobs.build.steps[1]",
                        "UNPINNED_ACTION @ jobs.build.steps[2]",
                        "UNPINNED_ACTION @ jobs.build.steps[3]",
                        "UNPINNED_ACTION @ jobs.build.steps[4]"),
                run(yaml),
                "steps count from 0. A tag (@v46), a branch (@master), a 7-character short SHA and no @ at all are all unpinned. "
                        + "actions/ and github/ actions may use tags, docker:// is not checked, and a full 40-character SHA is pinned");
        Finding first = audit.audit(yaml).get(0);
        assertTrue(first.message().contains("tj-actions/changed-files@v46"),
                "the message should quote the uses: value, so the reader knows which line to fix. It was: " + first.message());
    }

    @Test
    @DisplayName("A job that calls a reusable workflow on a branch is UNPINNED_ACTION at the job")
    void reusableWorkflowMustBePinned() {
        String yaml = """
                on: push
                permissions:
                  contents: read
                jobs:
                  build:
                    uses: spoke/ci-templates/.github/workflows/maven.yml@main
                  deploy:
                    uses: spoke/ci-templates/.github/workflows/deploy.yml@%s
                    needs: build
                """.formatted(SHA);
        assertEquals(List.of("UNPINNED_ACTION @ jobs.build"), run(yaml),
                "a job-level uses: runs someone's workflow, so the same pin rule applies at jobs.build. "
                        + "A job that calls a reusable workflow cannot set timeout-minutes, so no NO_TIMEOUT for these jobs");
    }

    @Test
    @DisplayName("No top-level permissions is NO_PERMISSIONS, unless every job sets its own")
    void missingPermissions() {
        String noneAnywhere = """
                on: push
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    steps:
                      - run: ./mvnw -B verify
                """;
        assertEquals(List.of("NO_PERMISSIONS @ workflow"), run(noneAnywhere),
                "with no permissions: anywhere, the GITHUB_TOKEN gets the repository default, which may allow writes");

        String everyJob = """
                on: push
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    permissions:
                      contents: read
                    steps:
                      - run: ./mvnw -B verify
                  release:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    permissions:
                      contents: write
                    steps:
                      - run: ./release.sh
                """;
        assertEquals(List.of(), run(everyJob), "every job sets its own permissions, so a top-level block is not needed");

        String oneJobMissing = everyJob.replace("    permissions:\n      contents: write\n", "");
        assertEquals(List.of("NO_PERMISSIONS @ workflow"), run(oneJobMissing),
                "the release job sets no permissions and there is no top-level block, so it gets the default: NO_PERMISSIONS");
    }

    @Test
    @DisplayName("permissions: write-all is BROAD_PERMISSIONS, at the top and in a job")
    void writeAllIsTooBroad() {
        String yaml = """
                on: push
                permissions: write-all
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    steps:
                      - run: ./mvnw -B verify
                  release:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    permissions: write-all
                    steps:
                      - run: ./release.sh
                """;
        assertEquals(List.of("BROAD_PERMISSIONS @ workflow", "BROAD_PERMISSIONS @ jobs.release"), run(yaml),
                "write-all gives every write scope: report it at the top (workflow) and in the release job");
    }

    @Test
    @DisplayName("pull_request_target plus a checkout of the pull request's code is PWN_REQUEST, even with an unquoted on:")
    void pwnRequestIsFound() {
        String template = """
                on:
                  TRIGGER:
                    types: [opened, synchronize]
                permissions:
                  contents: read
                jobs:
                  test:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    steps:
                      - uses: actions/checkout@v5
                        with:
                          ref: ${{ github.event.pull_request.head.sha }}
                      - run: ./mvnw -B verify
                  label:
                    runs-on: ubuntu-latest
                    timeout-minutes: 5
                    steps:
                      - uses: actions/checkout@v5
                      - uses: actions/checkout@v5
                        with:
                          ref: ${{ github.head_ref }}
                """;
        assertEquals(List.of("PWN_REQUEST @ jobs.test.steps[0]", "PWN_REQUEST @ jobs.label.steps[1]"),
                run(template.replace("TRIGGER", "pull_request_target")),
                "pull_request_target has secrets and a write token. Checking out github.event.pull_request.head or github.head_ref "
                        + "runs the stranger's code with them. A checkout without ref: gets the base branch and is fine. "
                        + "Remember: SnakeYAML stores the unquoted key on as Boolean.TRUE");
        assertEquals(List.of(), run(template.replace("TRIGGER", "pull_request")),
                "plain pull_request runs without secrets and with a read-only token, so checking out the head is fine");
    }

    @Test
    @DisplayName("A run line that echoes or printfs a ${{ secrets.* }} value is SECRET_ECHOED, with or without spaces")
    void echoedSecretsAreFound() {
        String yaml = """
                on: push
                permissions:
                  contents: read
                jobs:
                  deploy:
                    runs-on: ubuntu-latest
                    timeout-minutes: 15
                    steps:
                      - run: echo "key is ${{ secrets.DEPLOY_KEY }}"
                      - run: |
                          ./mvnw -B package
                          printf '%s' "${{secrets.REGISTRY_TOKEN}}" | base64
                      - env:
                          TOKEN: ${{ secrets.REGISTRY_TOKEN }}
                        run: echo "token has ${#TOKEN} characters"
                      - run: |
                          echo "Deploying"
                          ./deploy.sh --token "${{ secrets.DEPLOY_KEY }}"
                """;
        assertEquals(List.of("SECRET_ECHOED @ jobs.deploy.steps[0]", "SECRET_ECHOED @ jobs.deploy.steps[1]"), run(yaml),
                "steps 0 and 1 print a secret: ${{ secrets. may have no space after {{. Step 2 reads it through env:, "
                        + "and in step 3 the echo and the secret are on different lines, so neither is SECRET_ECHOED");
    }

    @Test
    @DisplayName("A job without timeout-minutes is NO_TIMEOUT")
    void missingTimeout() {
        String yaml = """
                on: push
                permissions:
                  contents: read
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    timeout-minutes: 20
                    steps:
                      - run: ./mvnw -B verify
                  integration-tests:
                    runs-on: ubuntu-latest
                    steps:
                      - run: ./mvnw -B verify -Pit
                """;
        assertEquals(List.of("NO_TIMEOUT @ jobs.integration-tests"), run(yaml),
                "integration-tests has no timeout-minutes, so a hung test could burn 6 hours of runner time");
    }

    @Test
    @DisplayName("Findings come in order: the workflow first, then each job in file order, its own findings before its steps")
    void findingsComeInOrder() {
        String yaml = """
                on: [push, pull_request_target]
                jobs:
                  build:
                    runs-on: ubuntu-latest
                    steps:
                      - uses: some-org/setup-cache@v2
                        with:
                          key: maven
                      - uses: actions/checkout@v5
                        with:
                          ref: refs/pull/${{ github.event.number }}/merge
                      - run: echo ${{ secrets.NPM_TOKEN }}
                  release:
                    runs-on: ubuntu-latest
                    permissions: write-all
                    steps:
                      - uses: softprops/action-gh-release@v2
                """;
        assertEquals(List.of(
                        "NO_PERMISSIONS @ workflow",
                        "NO_TIMEOUT @ jobs.build",
                        "UNPINNED_ACTION @ jobs.build.steps[0]",
                        "PWN_REQUEST @ jobs.build.steps[1]",
                        "SECRET_ECHOED @ jobs.build.steps[2]",
                        "BROAD_PERMISSIONS @ jobs.release",
                        "NO_TIMEOUT @ jobs.release",
                        "UNPINNED_ACTION @ jobs.release.steps[0]"),
                run(yaml),
                "order: workflow findings; then per job: BROAD_PERMISSIONS, NO_TIMEOUT, then each step in order "
                        + "(UNPINNED_ACTION, PWN_REQUEST, SECRET_ECHOED). A refs/pull/ ref is the pull request's code too");
    }
}
