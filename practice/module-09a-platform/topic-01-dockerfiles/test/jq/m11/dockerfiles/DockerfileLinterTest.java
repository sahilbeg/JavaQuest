package jq.m11.dockerfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DockerfileLinterTest {

    /** The production Dockerfile from the lesson: it should give no findings at all. */
    static final String GOOD = """
            # syntax=docker/dockerfile:1
            FROM maven:3.9.11-eclipse-temurin-21 AS build
            WORKDIR /src
            COPY .mvn/ .mvn/
            COPY mvnw pom.xml ./
            RUN ./mvnw -q -B dependency:go-offline
            COPY src ./src
            RUN ./mvnw -q -B package -DskipTests \\
             && java -Djarmode=tools -jar target/spoke-api.jar extract --layers --destination /extracted

            FROM eclipse-temurin:21.0.8_9-jre
            WORKDIR /app
            RUN groupadd --system --gid 10001 spoke \\
             && useradd --system --uid 10001 --gid 10001 spoke
            COPY --from=build /extracted/dependencies/ ./
            COPY --from=build /extracted/spring-boot-loader/ ./
            COPY --from=build /extracted/snapshot-dependencies/ ./
            COPY --from=build /extracted/application/ ./
            USER 10001
            EXPOSE 8080 8081
            ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
            ENTRYPOINT ["java", "-jar", "spoke-api.jar"]
            """;

    /** Short labels such as "RUNS_AS_ROOT@11", so a failing check shows the whole list at a glance. */
    static List<String> labels(String dockerfile) {
        return DockerfileLinter.lint(dockerfile).stream().map(f -> f.rule() + "@" + f.line()).toList();
    }

    @Test
    @DisplayName("The production spoke-api Dockerfile is clean, and removing its USER line gives exactly one finding")
    void goodDockerfileIsClean() {
        assertEquals(List.of(), labels(GOOD), "the lesson's multi-stage Dockerfile breaks none of the rules");
        String noUser = GOOD.replace("USER 10001\n", "");
        assertEquals(List.of("RUNS_AS_ROOT@11"), labels(noUser),
                "with no USER in the final stage the app runs as root: report it on the final stage's FROM line, 11");
        for (Finding finding : DockerfileLinter.lint(noUser)) {
            assertFalse(finding.message() == null || finding.message().isBlank(), "every finding needs a message for a human");
        }
    }

    @Test
    @DisplayName("Theo's 1.1 GB Dockerfile gets every finding, sorted by line and then by rule")
    void theosDockerfile() {
        String theos = """
                # spoke-api image, version 1
                FROM eclipse-temurin:21-jdk
                RUN apt-get update && \\
                    apt-get install -y curl git maven
                ARG GITHUB_TOKEN
                ENV SPRING_PROFILES_ACTIVE=prod DB_PASSWORD=changeme
                WORKDIR /app
                COPY . .
                RUN mvn -q dependency:go-offline && mvn -q package -DskipTests
                ADD config/ /app/config/
                EXPOSE 8080
                CMD java -jar target/spoke-api.jar
                """;
        assertEquals(List.of(
                        "JDK_IN_FINAL_STAGE@2", "RUNS_AS_ROOT@2",
                        "APT_LISTS_NOT_CLEANED@3",
                        "SECRET_IN_ENV_OR_ARG@5", "SECRET_IN_ENV_OR_ARG@6",
                        "SOURCES_BEFORE_DEPENDENCIES@8",
                        "ADD_FOR_LOCAL_FILE@10",
                        "SHELL_FORM_COMMAND@12"),
                labels(theos),
                "sort by line; on the same line (2) follow the order of the Rule enum: JDK_IN_FINAL_STAGE before RUNS_AS_ROOT");
    }

    @Test
    @DisplayName("Empty files, comment-only files and files without FROM give no stage findings")
    void emptyAndCommentOnly() {
        assertEquals(List.of(), labels(""), "an empty Dockerfile has nothing to report");
        assertEquals(List.of(), labels("# TODO: write the Dockerfile\n\n# later\n"), "comments alone have nothing to report");
        assertEquals(List.of("SECRET_IN_ENV_OR_ARG@1"), labels("ARG NPM_TOKEN\n"),
                "with no FROM there are no stages, so no root or JDK findings. An ARG before any FROM is still checked");
    }

    @Test
    @DisplayName("Base images must have a version tag or a digest; scratch and earlier stage names are fine")
    void baseImagePinning() {
        String dockerfile = """
                FROM eclipse-temurin AS a
                FROM eclipse-temurin:latest AS b
                FROM eclipse-temurin:21.0.8_9-jre AS c
                FROM eclipse-temurin@sha256:0f1e2d3c4b5a69788796a5b4c3d2e1f00f1e2d3c4b5a69788796a5b4c3d2e1f0 AS d
                FROM localhost:5000/spoke/base AS e
                FROM c AS f
                FROM scratch AS g
                USER 10001
                """;
        assertEquals(List.of("BASE_IMAGE_NOT_PINNED@1", "BASE_IMAGE_NOT_PINNED@2", "BASE_IMAGE_NOT_PINNED@5"), labels(dockerfile),
                "line 1 has no tag, line 2 uses latest, and line 5's ':5000' is a registry port, not a tag, so it has no tag either. "
                        + "A tag (3), a digest (4), an earlier stage's name (6) and scratch (7) are all fine");
    }

    @Test
    @DisplayName("A JDK or build-tool image in the final stage is reported, even when reached through a stage name")
    void jdkInFinalStage() {
        assertEquals(List.of("JDK_IN_FINAL_STAGE@1"),
                labels("FROM eclipse-temurin:21-jdk\nUSER 10001\nENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]\n"),
                "a single stage on a -jdk image ships the compiler and every JDK tool to production");
        assertEquals(List.of("JDK_IN_FINAL_STAGE@1"), labels("FROM maven:3.9.11-eclipse-temurin-21\nUSER 10001\n"),
                "maven images are build images with a full JDK: the last part of the name is maven");
        assertEquals(List.of(),
                labels("FROM eclipse-temurin:21-jdk AS build\nRUN echo build\nFROM eclipse-temurin:21-jre\nUSER 10001\n"),
                "a JDK in the build stage is fine: only the final stage ships");
        assertEquals(List.of("JDK_IN_FINAL_STAGE@3"),
                labels("FROM eclipse-temurin:21-jdk AS base\nRUN echo tools\nFROM base\nUSER 10001\n"),
                "FROM base starts from the stage named base, whose image is a JDK: follow stage names back to the real image");
    }

    @Test
    @DisplayName("The final stage must end on a non-root USER; build-stage users and nonroot base images are handled")
    void runsAsRoot() {
        assertEquals(List.of("RUNS_AS_ROOT@4"),
                labels("FROM eclipse-temurin:21-jre\nUSER 10001\nRUN echo hi\nUSER root\n"),
                "the LAST USER decides: switching back to root on line 4 is reported on that line");
        assertEquals(List.of("RUNS_AS_ROOT@2"), labels("FROM eclipse-temurin:21-jre\nUSER 0:0\n"),
                "user id 0 is root, with or without a group after the colon");
        assertEquals(List.of("RUNS_AS_ROOT@3"),
                labels("FROM eclipse-temurin:21-jdk AS build\nUSER 10001\nFROM eclipse-temurin:21-jre\n"),
                "a USER in the build stage does not carry into the final stage: report the final FROM, line 3");
        assertEquals(List.of(), labels("FROM gcr.io/distroless/java21-debian12:nonroot\nENTRYPOINT [\"java\", \"-jar\", \"/app.jar\"]\n"),
                "a base image whose name contains nonroot already runs as a non-root user, so no USER is needed");
    }

    @Test
    @DisplayName("ADD is reported for local files and folders, not for URLs")
    void addForLocalFiles() {
        String dockerfile = """
                FROM eclipse-temurin:21-jre
                ADD target/spoke-api.jar /app/spoke-api.jar
                ADD https://downloads.example.com/agent.jar /opt/agent.jar
                ADD --chown=10001 config/ /app/config/
                USER 10001
                """;
        assertEquals(List.of("ADD_FOR_LOCAL_FILE@2", "ADD_FOR_LOCAL_FILE@4"), labels(dockerfile),
                "lines 2 and 4 add local paths (flags such as --chown are skipped); line 3 downloads from a URL, which is allowed");
    }

    @Test
    @DisplayName("Shell-form ENTRYPOINT and CMD are reported in the final stage only")
    void shellFormCommands() {
        String dockerfile = """
                FROM maven:3.9.11-eclipse-temurin-21 AS build
                CMD mvn -v
                FROM eclipse-temurin:21-jre
                USER 10001
                ENTRYPOINT java -jar /app/spoke-api.jar
                CMD --spring.profiles.active=prod
                """;
        assertEquals(List.of("SHELL_FORM_COMMAND@5", "SHELL_FORM_COMMAND@6"), labels(dockerfile),
                "lines 5 and 6 are in shell form (no [ at the start). The CMD on line 2 is in a build stage that never runs");
        assertEquals(List.of(), labels("FROM eclipse-temurin:21-jre\nUSER 10001\nENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]\nCMD [\"--server.port=8080\"]\n"),
                "exec form, starting with [, is fine");
    }

    @Test
    @DisplayName("Copying the sources before Maven downloads dependencies is reported, unless a cache mount is used")
    void sourcesBeforeDependencies() {
        String early = """
                FROM maven:3.9.11-eclipse-temurin-21 AS build
                WORKDIR /src
                COPY pom.xml .
                COPY src ./src
                RUN mvn -q dependency:go-offline
                RUN mvn -q package
                FROM eclipse-temurin:21-jre
                COPY --from=build /src/target/spoke-api.jar .
                COPY . /app/
                USER 10001
                """;
        assertEquals(List.of("SOURCES_BEFORE_DEPENDENCIES@4"), labels(early),
                "line 4 copies src before the go-offline step on line 5. The final stage runs no mvn, so its COPY . is not checked");

        String cacheMount = """
                FROM maven:3.9.11-eclipse-temurin-21 AS build
                COPY . .
                RUN --mount=type=cache,target=/root/.m2 mvn -q package
                FROM eclipse-temurin:21-jre
                USER 10001
                """;
        assertEquals(List.of(), labels(cacheMount),
                "a RUN with --mount=type=cache keeps Maven's downloads between builds, so the order does not matter here");
    }

    @Test
    @DisplayName("ENV and ARG names that look like secrets are reported, one finding per name; apt lists must be cleaned")
    void secretsAndAptLists() {
        String dockerfile = """
                ARG REPO_TOKEN
                FROM eclipse-temurin:21-jre
                ENV SPRING_PROFILES_ACTIVE=prod DB_PASSWORD=changeme Stripe_Api_Key=sk_test_1
                ENV PAYMENT_SECRET hunter2
                ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Dspoke.region=riverton"
                ARG BUILD_NUMBER=0
                RUN apt-get update \\
                 && apt-get install -y --no-install-recommends tzdata
                RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
                USER 10001
                """;
        assertEquals(List.of(
                        "SECRET_IN_ENV_OR_ARG@1",
                        "SECRET_IN_ENV_OR_ARG@3", "SECRET_IN_ENV_OR_ARG@3",
                        "SECRET_IN_ENV_OR_ARG@4",
                        "APT_LISTS_NOT_CLEANED@7"),
                labels(dockerfile),
                "REPO_TOKEN (line 1, before FROM), DB_PASSWORD and Stripe_Api_Key (two names on line 3, case does not matter), "
                        + "and PAYMENT_SECRET in the old ENV NAME value form (line 4). JAVA_TOOL_OPTIONS's quoted value is one word. "
                        + "The RUN on lines 7-8 installs without removing /var/lib/apt/lists; the one on line 9 cleans up");
    }
}
