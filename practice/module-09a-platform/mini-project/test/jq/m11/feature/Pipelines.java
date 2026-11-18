package jq.m11.feature;

import java.util.List;

/** Spoke's pipeline, as YAML and as objects. */
final class Pipelines {

    static final String SPOKE_YAML = """
            name: spoke-api
            stages:
              - name: build
                type: build
              - name: test
                type: test
              - name: image
                type: image
              - name: deploy-staging
                type: deploy
              - name: smoke-check
                type: smoke
              - name: approval
                type: approval
                expiresAfterMinutes: 60
              - name: canary
                type: canary
                percent: 10
              - name: analysis
                type: analysis
                maxErrorRateIncrease: 0.5
                maxP99IncreasePercent: 20
              - name: promote
                type: promote
            """;

    static final List<String> BEFORE_APPROVAL = List.of("build", "test", "image", "deploy-staging", "smoke-check");

    static Pipeline spoke() {
        return new Pipeline("spoke-api", List.of(
                Stage.of("build", StageType.BUILD),
                Stage.of("test", StageType.TEST),
                Stage.of("image", StageType.IMAGE),
                Stage.of("deploy-staging", StageType.DEPLOY),
                Stage.of("smoke-check", StageType.SMOKE),
                Stage.approval("approval", 60),
                Stage.canary("canary", 10),
                Stage.analysis("analysis", 0.5, 20),
                Stage.of("promote", StageType.PROMOTE)));
    }

    private Pipelines() {
    }
}
