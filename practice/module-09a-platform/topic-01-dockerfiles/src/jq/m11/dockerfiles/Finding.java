package jq.m11.dockerfiles;

/** One problem found in a Dockerfile: which rule, on which line (1-based), and a message for a human. */
public record Finding(Rule rule, int line, String message) {

    @Override
    public String toString() {
        return rule + "@" + line + ": " + message;
    }
}
