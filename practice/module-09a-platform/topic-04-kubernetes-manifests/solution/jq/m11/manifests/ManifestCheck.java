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

        // Deployment rules
        Map<String, Object> matchLabels = map(path(deployment, "spec", "selector", "matchLabels"));
        if (!selects(matchLabels, podLabels)) {
            findings.add(new Finding(Rule.SELECTOR_MISMATCH, deploymentName));
        }
        Map<String, Object> hpa = autoscalerFor(documents, deploymentName);
        Object replicas = hpa != null ? path(hpa, "spec", "minReplicas") : path(deployment, "spec", "replicas");
        if (number(replicas, 1) < 2) {
            findings.add(new Finding(Rule.TOO_FEW_REPLICAS, deploymentName));
        }

        // Container rules
        Object podNonRoot = path(podSpec, "securityContext", "runAsNonRoot");
        for (Map<String, Object> container : containers) {
            String name = text(container.get("name"));
            if (!pinned(text(container.get("image")))) {
                findings.add(new Finding(Rule.IMAGE_NOT_PINNED, name));
            }
            if (container.get("readinessProbe") == null) {
                findings.add(new Finding(Rule.MISSING_READINESS_PROBE, name));
            }
            if (container.get("livenessProbe") == null) {
                findings.add(new Finding(Rule.MISSING_LIVENESS_PROBE, name));
            } else if (checksDependency(text(path(container, "livenessProbe", "httpGet", "path")))) {
                findings.add(new Finding(Rule.LIVENESS_CHECKS_DEPENDENCY, name));
            }
            Map<String, Object> requests = map(path(container, "resources", "requests"));
            if (requests.get("cpu") == null || requests.get("memory") == null) {
                findings.add(new Finding(Rule.MISSING_REQUESTS, name));
            }
            if (path(container, "resources", "limits", "memory") == null) {
                findings.add(new Finding(Rule.MISSING_MEMORY_LIMIT, name));
            }
            Map<String, Object> securityContext = map(container.get("securityContext"));
            Object nonRoot = securityContext.containsKey("runAsNonRoot") ? securityContext.get("runAsNonRoot") : podNonRoot;
            if (!Boolean.TRUE.equals(nonRoot)) {
                findings.add(new Finding(Rule.NOT_RUN_AS_NON_ROOT, name));
            }
        }

        // Service rules
        Set<String> portNumbers = new LinkedHashSet<>();
        Set<String> portNames = new LinkedHashSet<>();
        for (Map<String, Object> container : containers) {
            for (Map<String, Object> port : maps(container.get("ports"))) {
                if (port.get("containerPort") != null) {
                    portNumbers.add(text(port.get("containerPort")));
                }
                if (port.get("name") != null) {
                    portNames.add(text(port.get("name")));
                }
            }
        }
        for (Map<String, Object> service : all(documents, "Service")) {
            String name = text(path(service, "metadata", "name"));
            if (!selects(map(path(service, "spec", "selector")), podLabels)) {
                findings.add(new Finding(Rule.SERVICE_SELECTS_NOTHING, name));
            }
            for (Map<String, Object> port : maps(path(service, "spec", "ports"))) {
                Object target = port.get("targetPort") != null ? port.get("targetPort") : port.get("port");
                boolean known = target instanceof Number ? portNumbers.contains(text(target)) : portNames.contains(text(target));
                if (!known) {
                    findings.add(new Finding(Rule.TARGET_PORT_UNKNOWN, name));
                }
            }
        }
        return new ArrayList<>(findings);
    }

    /** True when the selector is not empty and every one of its labels is on the Pod, with the same value. */
    static boolean selects(Map<String, Object> selector, Map<String, Object> podLabels) {
        if (selector.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, Object> label : selector.entrySet()) {
            if (!podLabels.containsKey(label.getKey()) || !Objects.equals(text(label.getValue()), text(podLabels.get(label.getKey())))) {
                return false;
            }
        }
        return true;
    }

    /** True for an image with a digest, or with a tag that is not latest. Looks for the tag after the last '/'. */
    static boolean pinned(String image) {
        if (image == null || image.isBlank()) {
            return false;
        }
        if (image.contains("@sha256:")) {
            return true;
        }
        String lastPart = image.substring(image.lastIndexOf('/') + 1);
        int colon = lastPart.indexOf(':');
        return colon >= 0 && !lastPart.substring(colon + 1).equals("latest");
    }

    /** True for /actuator/health itself, or any path with a db, database, redis or kafka segment. */
    static boolean checksDependency(String path) {
        if (path == null) {
            return false;
        }
        String clean = path.toLowerCase();
        int query = clean.indexOf('?');
        if (query >= 0) {
            clean = clean.substring(0, query);
        }
        while (clean.endsWith("/") && clean.length() > 1) {
            clean = clean.substring(0, clean.length() - 1);
        }
        if (clean.equals("/actuator/health")) {
            return true;
        }
        for (String segment : clean.split("/")) {
            if (DEPENDENCY_SEGMENTS.contains(segment)) {
                return true;
            }
        }
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
