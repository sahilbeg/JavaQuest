package jq.m11.feature;

/** Reads the canary's numbers and the baseline's (the old version, over the same minutes). */
public interface MetricsSource {

    Metrics baseline();

    Metrics canary();
}
