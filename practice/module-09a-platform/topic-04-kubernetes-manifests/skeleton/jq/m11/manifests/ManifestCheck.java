package jq.m11.manifests;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Checks a Deployment, its Services and an optional HorizontalPodAutoscaler before they reach the cluster. */
public final class ManifestCheck {

    private static final Set<String> DEPENDENCY_SEGMENTS = Set.of("db", "database", "redis", "kafka");

    private ManifestCheck() {
    }

    /** Every problem found in a multi-document YAML file, each once, in any order. */
    public static List<Finding> check(String yaml) {
        List<Map<String, Object>> documents = loadDocuments(yaml);
        Map<String, Object> deployment = only(documents, "Deployment");
        String deploymentName = text(path(deployment, "metadata", "name"));
        Map<String, Object> podLabels = map(path(deployment, "spec", "template", "metadata", "labels"));
        Map<String, Object> podSpec = map(path(deployment, "spec", "template", "spec"));
        List<Map<String, Object>> containers = maps(podSpec.get("containers"));

        Set<Finding> findings = new LinkedHashSet<>();

        // TODO 4: Deployment rules, with deploymentName as the subject.
        //         SELECTOR_MISMATCH when selects(spec.selector.matchLabels, podLabels) is false.
        //         TOO_FEW_REPLICAS when the replica count is below 2. The count is the minReplicas of
        //         autoscalerFor(documents, deploymentName) if there is one, else spec.replicas; missing means 1.

        for (Map<String, Object> container : containers) {
            String name = text(container.get("name"));
            // TODO 5: with name as the subject: IMAGE_NOT_PINNED (use pinned), MISSING_READINESS_PROBE,
            //         MISSING_LIVENESS_PROBE, and LIVENESS_CHECKS_DEPENDENCY (use checksDependency on livenessProbe.httpGet.path).
            // TODO 6: MISSING_REQUESTS (resources.requests needs cpu and memory), MISSING_MEMORY_LIMIT (resources.limits.memory),
            //         NOT_RUN_AS_NON_ROOT: the container's securityContext.runAsNonRoot if the key is there, else the Pod's
            //         spec.securityContext.runAsNonRoot; anything but true is a finding.
        }

        for (Map<String, Object> service : all(documents, "Service")) {
            String name = text(path(service, "metadata", "name"));
            // TODO 7: with name as the subject: SERVICE_SELECTS_NOTHING when selects(spec.selector, podLabels) is false.
            //         TARGET_PORT_UNKNOWN for a spec.ports entry whose targetPort (or port, when targetPort is missing)
            //         is a number that no container's containerPort has, or a name that no container port has.
        }
        return new ArrayList<>(findings);
    }

    /** True when the selector is not empty and every one of its labels is on the Pod, with the same value. */
    static boolean selects(Map<String, Object> selector, Map<String, Object> podLabels) {
        // TODO 1: false for an empty selector. Otherwise true only if every key is in podLabels with an equal value.
        //         Compare values with Objects.equals(text(a), text(b)), because YAML may read a label as a number.
        return true;
    }

    /** True for an image with a digest, or with a tag that is not latest. Looks for the tag after the last '/'. */
    static boolean pinned(String image) {
        // TODO 2: false for null or blank. true if it contains "@sha256:". Otherwise take the part after the last '/':
        //         it is pinned when it has a ':' and the text after that ':' is not "latest".
        return true;
    }

    /** True for /actuator/health itself, or any path with a db, database, redis or kafka segment. */
    static boolean checksDependency(String path) {
        // TODO 3: false for null. Lower-case it, drop any "?query" and trailing '/', then: true if it equals
        //         "/actuator/health", or if any segment between '/' characters is in DEPENDENCY_SEGMENTS.
        return false;
    }

    // ---- Helpers, given to you, finished ----

    /** Every non-empty document in the file, as maps. */
    static List<Map<String, Object>> loadDocuments(String yaml) {
        List<Map<String, Object>> documents = new ArrayList<>();
        for (Object document : new Yaml(new SafeConstructor(new LoaderOptions())).loadAll(yaml)) {
            if (document != null) {
                documents.add(map(document));
            }
        }
        return documents;
    }

    /** The one document of this kind; IllegalArgumentException if there is none or more than one. */
    static Map<String, Object> only(List<Map<String, Object>> documents, String kind) {
        List<Map<String, Object>> found = all(documents, kind);
        if (found.size() != 1) {
            throw new IllegalArgumentException("expected exactly one " + kind + ", found " + found.size());
        }
        return found.getFirst();
    }

    /** Every document of this kind, in file order. */
    static List<Map<String, Object>> all(List<Map<String, Object>> documents, String kind) {
        return documents.stream().filter(d -> kind.equals(d.get("kind"))).toList();
    }

    /** The HorizontalPodAutoscaler whose scaleTargetRef is this Deployment, or null. */
    static Map<String, Object> autoscalerFor(List<Map<String, Object>> documents, String deploymentName) {
        for (Map<String, Object> hpa : all(documents, "HorizontalPodAutoscaler")) {
            if ("Deployment".equals(path(hpa, "spec", "scaleTargetRef", "kind"))
                    && Objects.equals(deploymentName, text(path(hpa, "spec", "scaleTargetRef", "name")))) {
                return hpa;
            }
        }
        return null;
    }

    /** Walks nested maps: path(doc, "spec", "template") is doc.spec.template, or null if any step is missing. */
    static Object path(Map<String, Object> start, String... keys) {
        Object current = start;
        for (String key : keys) {
            if (!(current instanceof Map<?, ?> m)) {
                return null;
            }
            current = m.get(key);
        }
        return current;
    }

    /** The value as a map, or an empty map when it is missing or not a map. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    /** The value as a list of maps, skipping anything that is not a map; empty when missing. */
    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(Map.class::isInstance).map(item -> (Map<String, Object>) item).toList();
    }

    /** The value as text: 8080 becomes "8080", true becomes "true", null stays null. */
    static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /** The value as a whole number, or the default when it is missing or not a number. */
    static int number(Object value, int whenMissing) {
        return value instanceof Number n ? n.intValue() : whenMissing;
    }
}
