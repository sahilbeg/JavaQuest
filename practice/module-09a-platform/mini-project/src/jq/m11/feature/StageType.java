package jq.m11.feature;

/** The kinds of stage a pipeline can have. In the YAML they are written in lowercase, such as {@code canary}. */
public enum StageType {
    BUILD, TEST, IMAGE, DEPLOY, SMOKE, APPROVAL, CANARY, ANALYSIS, PROMOTE
}
