package jq.m11.manifests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ManifestCheckTest {

    /** A Deployment and a Service that break no rule. Each test changes one part of it. */
    static final String GOOD = """
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              name: spoke-api
              namespace: spoke
            spec:
              replicas: 3
              selector:
                matchLabels:
                  app: spoke-api
              template:
                metadata:
                  labels:
                    app: spoke-api
                    track: stable
                spec:
                  securityContext:
                    runAsNonRoot: true
                  containers:
                    - name: api
                      image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e
                      ports:
                        - name: http
                          containerPort: 8080
                        - name: management
                          containerPort: 8081
                      readinessProbe:
                        httpGet:
                          path: /actuator/health/readiness
                          port: management
                      livenessProbe:
                        httpGet:
                          path: /actuator/health/liveness
                          port: management
                      resources:
                        requests:
                          cpu: 500m
                          memory: 1Gi
                        limits:
                          memory: 1Gi
            ---
            apiVersion: v1
            kind: Service
            metadata:
              name: spoke-api
              namespace: spoke
            spec:
              selector:
                app: spoke-api
              ports:
                - name: http
                  port: 80
                  targetPort: http
            """;

    static Set<Finding> findings(String yaml) {
        List<Finding> list = ManifestCheck.check(yaml);
        Set<Finding> set = new HashSet<>(list);
        assertEquals(set.size(), list.size(), "each finding should appear only once, but check() returned " + list);
        return set;
    }

    static String change(String yaml, String from, String to) {
        assertTrue(yaml.contains(from), "test setup: the manifest should contain " + from);
        return yaml.replace(from, to);
    }

    @Test
    @DisplayName("A Deployment and Service that follow every rule give no findings")
    void goodManifestHasNoFindings() {
        assertEquals(Set.of(), findings(GOOD), "this manifest breaks no rule, so check() should return an empty list");
    }

    @Test
    @DisplayName("A Deployment selector that is not in the Pod template's labels is reported; extra Pod labels are fine")
    void deploymentSelectorMustMatchTemplate() {
        String wrong = change(GOOD, "    matchLabels:\n      app: spoke-api\n", "    matchLabels:\n      app: spoke-api-v2\n");
        assertTrue(findings(wrong).contains(new Finding(Rule.SELECTOR_MISMATCH, "spoke-api")),
                "matchLabels app=spoke-api-v2 is not on the Pods (app=spoke-api), so expect SELECTOR_MISMATCH for spoke-api");

        String subset = change(GOOD, "    matchLabels:\n      app: spoke-api\n", "    matchLabels:\n      app: spoke-api\n      track: stable\n");
        assertFalse(findings(subset).contains(new Finding(Rule.SELECTOR_MISMATCH, "spoke-api")),
                "app and track are both on the Pods, so this selector matches: no SELECTOR_MISMATCH");
    }

    @Test
    @DisplayName("A Service whose selector matches no Pod label is reported, the bug from the ticket")
    void serviceSelectorMustMatchPods() {
        String wrong = change(GOOD, "  selector:\n    app: spoke-api\n  ports:", "  selector:\n    app: spoke\n  ports:");
        assertEquals(Set.of(new Finding(Rule.SERVICE_SELECTS_NOTHING, "spoke-api")), findings(wrong),
                "the Pods are labelled app=spoke-api, not app=spoke, so the Service selects nothing");

        String twoLabels = change(GOOD, "  selector:\n    app: spoke-api\n  ports:", "  selector:\n    app: spoke-api\n    track: canary\n  ports:");
        assertEquals(Set.of(new Finding(Rule.SERVICE_SELECTS_NOTHING, "spoke-api")), findings(twoLabels),
                "every selector label must match: the Pods have track=stable, not track=canary");

        String none = change(GOOD, "  selector:\n    app: spoke-api\n  ports:", "  ports:");
        assertEquals(Set.of(new Finding(Rule.SERVICE_SELECTS_NOTHING, "spoke-api")), findings(none),
                "a Service with no selector sends traffic to none of the Deployment's Pods");
    }

    @Test
    @DisplayName("A Service targetPort must be a containerPort number or a port name; without one, it is the same as port")
    void targetPortMustExistOnThePod() {
        assertEquals(Set.of(new Finding(Rule.TARGET_PORT_UNKNOWN, "spoke-api")),
                findings(change(GOOD, "targetPort: http", "targetPort: 9090")), "no container listens on 9090");
        assertEquals(Set.of(new Finding(Rule.TARGET_PORT_UNKNOWN, "spoke-api")),
                findings(change(GOOD, "targetPort: http", "targetPort: web")), "no container port is named web");
        assertEquals(Set.of(), findings(change(GOOD, "targetPort: http", "targetPort: 8080")),
                "8080 is a containerPort, so a number works as well as the name http");
        assertEquals(Set.of(new Finding(Rule.TARGET_PORT_UNKNOWN, "spoke-api")),
                findings(change(GOOD, "      targetPort: http\n", "")),
                "with no targetPort, Kubernetes uses port (80), and no container listens on 80");
        assertEquals(Set.of(), findings(change(GOOD, "      port: 80\n      targetPort: http\n", "      port: 8080\n")),
                "with no targetPort and port 8080, traffic goes to 8080, which is a containerPort");
    }

    @Test
    @DisplayName("Images with no tag or the tag latest are reported; a version tag or a digest is pinned")
    void imagesMustBePinned() {
        String image = "image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e";
        Finding notPinned = new Finding(Rule.IMAGE_NOT_PINNED, "api");
        assertEquals(Set.of(notPinned), findings(change(GOOD, image, "image: ghcr.io/spoke/spoke-api:latest")),
                "the tag latest moves to every new build, so it is not pinned");
        assertEquals(Set.of(notPinned), findings(change(GOOD, image, "image: ghcr.io/spoke/spoke-api")),
                "no tag at all means latest");
        assertEquals(Set.of(notPinned), findings(change(GOOD, image, "image: localhost:5000/spoke-api")),
                "the :5000 is the registry's port, not a tag: look for the tag only after the last /");
        assertEquals(Set.of(), findings(change(GOOD, image, "image: localhost:5000/spoke-api:2.6.0-3f9c2b7a1d4e")),
                "localhost:5000/spoke-api:2.6.0-3f9c2b7a1d4e has the tag 2.6.0-3f9c2b7a1d4e");
        assertEquals(Set.of(), findings(change(GOOD, image,
                "image: ghcr.io/spoke/spoke-api@sha256:3f9c2e1a7b8d4c6e5f0a1b2c3d4e5f60718293a4b5c6d7e8f9a0b1c2d3e4f5a6")),
                "a digest names one exact build, so it is pinned");
    }

    @Test
    @DisplayName("Fewer than 2 replicas is reported, and a missing replicas counts as 1")
    void needsAtLeastTwoReplicas() {
        Finding tooFew = new Finding(Rule.TOO_FEW_REPLICAS, "spoke-api");
        assertEquals(Set.of(tooFew), findings(change(GOOD, "replicas: 3", "replicas: 1")),
                "one Pod means any restart is an outage");
        assertEquals(Set.of(tooFew), findings(change(GOOD, "  replicas: 3\n", "")),
                "without replicas, Kubernetes runs 1 Pod");
        assertEquals(Set.of(), findings(change(GOOD, "replicas: 3", "replicas: 2")), "2 replicas is enough");
    }

    @Test
    @DisplayName("When an HPA targets the Deployment, its minReplicas counts instead of replicas")
    void autoscalerMinReplicasCounts() {
        String hpa = """
                ---
                apiVersion: autoscaling/v2
                kind: HorizontalPodAutoscaler
                metadata:
                  name: spoke-api
                spec:
                  scaleTargetRef:
                    apiVersion: apps/v1
                    kind: Deployment
                    name: spoke-api
                  minReplicas: MIN
                  maxReplicas: 30
                """;
        String noReplicas = change(GOOD, "  replicas: 3\n", "");
        assertEquals(Set.of(), findings(noReplicas + hpa.replace("MIN", "3")),
                "the HPA keeps at least 3 Pods, so leaving replicas out is right");
        assertEquals(Set.of(new Finding(Rule.TOO_FEW_REPLICAS, "spoke-api")), findings(GOOD + hpa.replace("MIN", "1")),
                "the HPA may shrink spoke-api to 1 Pod, even though replicas says 3");
        assertEquals(Set.of(new Finding(Rule.TOO_FEW_REPLICAS, "spoke-api")),
                findings(noReplicas + hpa.replace("MIN", "3").replace("name: spoke-api\n  minReplicas", "name: billing\n  minReplicas")),
                "this HPA scales billing, not spoke-api, so spoke-api's missing replicas still means 1");
    }

    @Test
    @DisplayName("Missing probes are reported for each container by name, including a sidecar")
    void missingProbesPerContainer() {
        String noReadiness = change(GOOD, """
                          readinessProbe:
                            httpGet:
                              path: /actuator/health/readiness
                              port: management
                """, "");
        assertEquals(Set.of(new Finding(Rule.MISSING_READINESS_PROBE, "api")), findings(noReadiness),
                "container api has no readinessProbe");

        String withSidecar = change(GOOD, "          resources:\n", """
                          resources:
                            requests:
                              cpu: 500m
                              memory: 1Gi
                            limits:
                              memory: 1Gi
                        - name: log-shipper
                          image: ghcr.io/spoke/log-shipper:1.4.2
                          resources:
                """);
        Set<Finding> found = findings(withSidecar);
        assertTrue(found.contains(new Finding(Rule.MISSING_READINESS_PROBE, "log-shipper")),
                "the second container, log-shipper, has no readinessProbe: expect a finding named log-shipper, got " + found);
        assertTrue(found.contains(new Finding(Rule.MISSING_LIVENESS_PROBE, "log-shipper")),
                "log-shipper has no livenessProbe either, got " + found);
        assertFalse(found.contains(new Finding(Rule.MISSING_LIVENESS_PROBE, "api")),
                "container api still has its probes: check every container on its own");
    }

    @Test
    @DisplayName("A liveness probe on /actuator/health or a db, database, redis or kafka path is reported")
    void livenessMustNotCheckDependencies() {
        Finding dependency = new Finding(Rule.LIVENESS_CHECKS_DEPENDENCY, "api");
        String liveness = "path: /actuator/health/liveness";
        assertEquals(Set.of(dependency), findings(change(GOOD, liveness, "path: /actuator/health")),
                "the full /actuator/health includes the database check");
        assertEquals(Set.of(dependency), findings(change(GOOD, liveness, "path: /actuator/health/")),
                "a trailing / is still the full health check");
        assertEquals(Set.of(dependency), findings(change(GOOD, liveness, "path: /actuator/health/db")),
                "/actuator/health/db checks the database");
        assertEquals(Set.of(dependency), findings(change(GOOD, liveness, "path: /internal/redis/ping")),
                "a path with a redis segment checks Redis");
        assertEquals(Set.of(), findings(change(GOOD, liveness, "path: /actuator/health/liveness?full=1")),
                "the liveness group checks only the app itself");
        assertEquals(Set.of(), findings(change(GOOD, liveness, "path: /feedback")),
                "/feedback contains the letters db but has no db segment: compare whole segments");
        assertEquals(Set.of(), findings(change(GOOD, "path: /actuator/health/readiness", "path: /actuator/health")),
                "the rule is for liveness only: readiness may look at the database");
    }

    @Test
    @DisplayName("Missing CPU or memory requests, and a missing memory limit, are reported")
    void resourcesAreRequired() {
        assertEquals(Set.of(new Finding(Rule.MISSING_REQUESTS, "api")),
                findings(change(GOOD, "              cpu: 500m\n", "")),
                "a CPU request is needed: the scheduler and the HPA use it");
        assertEquals(Set.of(new Finding(Rule.MISSING_MEMORY_LIMIT, "api")),
                findings(change(GOOD, "            limits:\n              memory: 1Gi\n", "")),
                "without a memory limit, one leaking Pod can starve its whole node");
        assertEquals(Set.of(new Finding(Rule.MISSING_REQUESTS, "api"), new Finding(Rule.MISSING_MEMORY_LIMIT, "api")),
                findings(change(GOOD, "          resources:\n            requests:\n              cpu: 500m\n              memory: 1Gi\n            limits:\n              memory: 1Gi\n", "")),
                "no resources at all: report MISSING_REQUESTS once and MISSING_MEMORY_LIMIT once");
    }

    @Test
    @DisplayName("runAsNonRoot must be true: the container's setting wins over the Pod's")
    void mustRunAsNonRoot() {
        Finding root = new Finding(Rule.NOT_RUN_AS_NON_ROOT, "api");
        assertEquals(Set.of(root), findings(change(GOOD, "      securityContext:\n        runAsNonRoot: true\n", "")),
                "nothing says runAsNonRoot: true, so the container may run as root");
        assertEquals(Set.of(root), findings(change(GOOD, "          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e\n",
                "          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e\n          securityContext:\n            runAsNonRoot: false\n")),
                "the container sets runAsNonRoot: false, which overrides the Pod's true");
        assertEquals(Set.of(), findings(change(GOOD.replace("runAsNonRoot: true", "runAsNonRoot: false"),
                "          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e\n",
                "          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e\n          securityContext:\n            runAsNonRoot: true\n")),
                "the container sets runAsNonRoot: true, which overrides the Pod's false");
    }

    @Test
    @DisplayName("Empty documents are skipped, and a file with no Deployment is an error")
    void needsExactlyOneDeployment() {
        assertEquals(Set.of(), findings("---\n" + GOOD + "---\n"), "empty documents between --- lines should be skipped");
        String serviceOnly = GOOD.substring(GOOD.indexOf("apiVersion: v1"));
        assertThrows(IllegalArgumentException.class, () -> ManifestCheck.check(serviceOnly),
                "a file with no Deployment cannot be checked: throw IllegalArgumentException");
    }
}
