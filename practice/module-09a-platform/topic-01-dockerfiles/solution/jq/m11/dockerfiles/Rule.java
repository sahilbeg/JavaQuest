package jq.m11.dockerfiles;

/** The rules the linter checks. Findings on the same line are reported in this order. */
public enum Rule {
    /** A FROM image with no tag and no digest, or with the tag latest. */
    BASE_IMAGE_NOT_PINNED,
    /** The final stage runs on a JDK or a build-tool image (maven, gradle) instead of a runtime. */
    JDK_IN_FINAL_STAGE,
    /** The final stage has no USER, or its last USER is root (or 0). */
    RUNS_AS_ROOT,
    /** ADD used for a local file or folder: COPY does the same, without ADD's surprises. */
    ADD_FOR_LOCAL_FILE,
    /** ENTRYPOINT or CMD in shell form in the final stage: java is not PID 1 for sure, and may never get SIGTERM. */
    SHELL_FORM_COMMAND,
    /** The source code is copied before the dependencies are downloaded, so every code change downloads them again. */
    SOURCES_BEFORE_DEPENDENCIES,
    /** An ENV or ARG whose name looks like a secret: its value is saved in the image for anyone to read. */
    SECRET_IN_ENV_OR_ARG,
    /** apt-get install without removing /var/lib/apt/lists in the same RUN, so the package lists stay in the layer. */
    APT_LISTS_NOT_CLEANED
}
