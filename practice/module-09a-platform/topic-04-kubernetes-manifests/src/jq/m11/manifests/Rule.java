package jq.m11.manifests;

/** Every rule ManifestCheck knows. Given to you, finished. */
public enum Rule {
    /** The Deployment's spec.selector.matchLabels is empty, or not found in its Pod template's labels. */
    SELECTOR_MISMATCH,
    /** Fewer than 2 Pods: replicas (or the HPA's minReplicas) is below 2, or missing, which means 1. */
    TOO_FEW_REPLICAS,
    /** An image with no tag, or the tag latest, and no digest. */
    IMAGE_NOT_PINNED,
    /** A container with no readinessProbe. */
    MISSING_READINESS_PROBE,
    /** A container with no livenessProbe. */
    MISSING_LIVENESS_PROBE,
    /** A liveness probe whose path checks a dependency, such as /actuator/health or /actuator/health/db. */
    LIVENESS_CHECKS_DEPENDENCY,
    /** A container without both a CPU request and a memory request. */
    MISSING_REQUESTS,
    /** A container without a memory limit. */
    MISSING_MEMORY_LIMIT,
    /** A container that is not made to run as a non-root user. */
    NOT_RUN_AS_NON_ROOT,
    /** A Service whose selector is empty, or matches none of the Deployment's Pod labels. */
    SERVICE_SELECTS_NOTHING,
    /** A Service port whose targetPort is neither a containerPort number nor a port name of the Pod. */
    TARGET_PORT_UNKNOWN
}
