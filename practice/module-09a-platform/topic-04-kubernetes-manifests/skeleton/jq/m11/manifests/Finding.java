package jq.m11.manifests;

/**
 * One problem found in a manifest. Given to you, finished.
 *
 * @param rule    which rule was broken
 * @param subject who broke it: the container name for container rules, the Deployment name for
 *                SELECTOR_MISMATCH and TOO_FEW_REPLICAS, the Service name for Service rules
 */
public record Finding(Rule rule, String subject) {
}
