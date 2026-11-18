package dev.cadence.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * An animated explanation in a lesson: a ```animate block holding YAML. It plays step by step, with a short
 * caption per step, so a learner watches an idea happen instead of reading about it.
 *
 * <p>Two kinds:
 * <ul>
 *   <li>{@code code}: a Java program stepped line by line. Each step highlights a line and shows the variables,
 *       the objects they point to, and what the program printed so far.</li>
 *   <li>{@code scene}: up to eight labelled boxes. Steps show or highlight boxes, change their values, and send
 *       labelled tokens from one box to another, for ideas like "the CPU asks memory for a value".</li>
 * </ul>
 * The format is described in docs/CONTENT_SPEC.md. Everything is checked when the block is parsed, and the
 * content checker also runs a code animation's program and compares its output with the steps' {@code out}.
 */
public record Animation(String title, Kind kind, String code, List<Node> nodes, List<Link> links, List<Step> steps) {

    public enum Kind {
        @JsonProperty("code") CODE,
        @JsonProperty("scene") SCENE
    }

    /** A box in a scene. {@code row} and {@code col} place it on a small grid; by default boxes sit in one row. */
    public record Node(String id, String label, String emoji, String value, Integer row, Integer col) {}

    /** A line that always joins two boxes in a scene, like a folder and its parent, or two stations on a route. */
    public record Link(String from, String to, String label) {}

    /** A token that travels from one box to another, carrying a short label. {@code back} draws a reply. */
    public record Send(String from, String to, String label, Boolean back) {}

    /** An object on the heap, drawn as a card. Field values may point to other objects with "@id". */
    public record Obj(String type, Map<String, Object> fields) {}

    /**
     * One step. Code animations use line, vars, objects and out. Scenes use show, hide, focus, send and set.
     * Maps are changes: a key with an empty value (~) removes a variable or object.
     */
    public record Step(String say, Integer line, Map<String, Object> vars, Map<String, Obj> objects, String out,
                       List<String> show, List<String> hide, List<String> focus, List<Send> send,
                       Map<String, String> set) {}

    public static final int MAX_STEPS = 24;
    public static final int MAX_NODES = 8;
    public static final int MAX_SAY = 240;
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9-]*");
    /** A bare ~ (YAML's "nothing") as a map value, which the parser would otherwise read as the text "~". */
    private static final Pattern TILDE = Pattern.compile("(:[ \\t]*)~(?=[ \\t]*(?:[,}\\n#]|$))", Pattern.MULTILINE);

    private static final YAMLMapper YAML = YAMLMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .build();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    public Animation {
        require(title != null && !title.isBlank(), "title is required");
        require(kind != null, "kind is required: code or scene");
        require(steps != null && steps.size() >= 2 && steps.size() <= MAX_STEPS,
                "steps needs 2 to " + MAX_STEPS + " entries");
        steps = List.copyOf(steps);
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            String at = "step " + (i + 1) + ": ";
            require(step.say() != null && !step.say().isBlank(), at + "say is required");
            require(step.say().length() <= MAX_SAY, at + "say is longer than " + MAX_SAY + " characters; split the step");
        }
        if (kind == Kind.CODE) {
            require(code != null && !code.isBlank(), "a code animation needs code");
            require(nodes == null && links == null, "nodes and links belong to scene animations");
            checkCodeSteps(code.strip().split("\n", -1).length, steps);
        } else {
            require(code == null, "code belongs to code animations");
            require(nodes != null && nodes.size() >= 1 && nodes.size() <= MAX_NODES, "a scene needs 1 to " + MAX_NODES + " nodes");
            nodes = List.copyOf(nodes);
            links = links == null ? List.of() : List.copyOf(links);
            checkScene(nodes, steps);
            Set<String> ids = new HashSet<>(nodes.stream().map(Node::id).toList());
            for (Link link : links) {
                require(ids.contains(link.from()) && ids.contains(link.to()),
                        "links need from and to that name nodes, got " + link.from() + " - " + link.to());
            }
        }
    }

    private static void checkCodeSteps(int lineCount, List<Step> steps) {
        Set<String> objects = new HashSet<>();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            String at = "step " + (i + 1) + ": ";
            require(step.show() == null && step.hide() == null && step.focus() == null && step.send() == null && step.set() == null,
                    at + "show, hide, focus, send and set belong to scene animations");
            require(step.line() == null || (step.line() >= 0 && step.line() <= lineCount),
                    at + "line " + step.line() + " is outside the code (1 to " + lineCount + ")");
            if (step.objects() != null) {
                step.objects().forEach((id, obj) -> {
                    require(ID.matcher(id).matches(), at + "object id '" + id + "' must be lowercase letters, digits and dashes");
                    if (obj == null) {
                        objects.remove(id);
                    } else {
                        require(obj.type() != null && !obj.type().isBlank(), at + "object " + id + " needs a type");
                        objects.add(id);
                    }
                });
                step.objects().values().stream().filter(o -> o != null && o.fields() != null)
                        .flatMap(o -> o.fields().values().stream())
                        .forEach(v -> checkRef(v, objects, at));
            }
            if (step.vars() != null) {
                step.vars().values().forEach(v -> checkRef(v, objects, at));
            }
        }
    }

    private static void checkRef(Object value, Set<String> objects, String at) {
        if (value instanceof String s && s.startsWith("@")) {
            require(objects.contains(s.substring(1)), at + "'" + s + "' points to an object that does not exist yet");
        }
    }

    private static void checkScene(List<Node> nodes, List<Step> steps) {
        Set<String> ids = new HashSet<>();
        for (Node node : nodes) {
            require(node.id() != null && ID.matcher(node.id()).matches(),
                    "node id '" + node.id() + "' must be lowercase letters, digits and dashes");
            require(ids.add(node.id()), "node id '" + node.id() + "' is used twice");
            require(node.label() != null && !node.label().isBlank(), "node " + node.id() + " needs a label");
            require(node.row() == null || (node.row() >= 0 && node.row() <= 3), "node " + node.id() + ": row is 0 to 3");
            require(node.col() == null || (node.col() >= 0 && node.col() <= 5), "node " + node.id() + ": col is 0 to 5");
        }
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            String at = "step " + (i + 1) + ": ";
            require(step.line() == null && step.vars() == null && step.objects() == null && step.out() == null,
                    at + "line, vars, objects and out belong to code animations");
            for (List<String> list : List.of(nz(step.show()), nz(step.hide()), nz(step.focus()))) {
                list.forEach(id -> require(ids.contains(id), at + "no node called '" + id + "'"));
            }
            for (Send send : nz(step.send())) {
                require(ids.contains(send.from()) && ids.contains(send.to()),
                        at + "send needs from and to that name nodes, got " + send.from() + " -> " + send.to());
                require(!send.from().equals(send.to()), at + "send goes from a node to itself");
            }
            if (step.set() != null) {
                step.set().keySet().forEach(id -> require(ids.contains(id), at + "set names no node called '" + id + "'"));
            }
        }
    }

    /** Parses a ```animate block. Throws IllegalArgumentException with a message that names the problem. */
    public static Animation parse(String yaml) {
        try {
            return YAML.readValue(TILDE.matcher(yaml).replaceAll("$1null"), Animation.class);
        } catch (JacksonException e) {
            Throwable cause = e;
            while (cause.getCause() != null && !(cause instanceof IllegalArgumentException)) {
                cause = cause.getCause();
            }
            String message = cause instanceof IllegalArgumentException ? cause.getMessage() : e.getOriginalMessage();
            throw new IllegalArgumentException("animate block: " + message, e);
        }
    }

    /** The animation as JSON, for the page to play. */
    public String toJson() {
        return JSON.writeValueAsString(this);
    }

    /** Everything a code animation says the program prints, in order, one {@code out} after another. */
    public String expectedOutput() {
        var out = new StringBuilder();
        for (Step step : steps) {
            if (step.out() != null) {
                out.append(step.out().endsWith("\n") ? step.out() : step.out() + "\n");
            }
        }
        return out.toString();
    }

    /** Variables after each step, for tests and for the page's text alternative. */
    public Map<String, Object> varsAfter(int stepIndex) {
        var vars = new LinkedHashMap<String, Object>();
        for (int i = 0; i <= stepIndex; i++) {
            Map<String, Object> change = steps.get(i).vars();
            if (change != null) {
                change.forEach((k, v) -> {
                    if (v == null) {
                        vars.remove(k);
                    } else {
                        vars.put(k, v);
                    }
                });
            }
        }
        return vars;
    }

    private static <T> List<T> nz(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
