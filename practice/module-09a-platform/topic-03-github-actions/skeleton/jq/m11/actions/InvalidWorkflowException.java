package jq.m11.actions;

/** Thrown when a workflow file cannot be read or planned: bad YAML, no jobs, an unknown need, a cycle. */
public class InvalidWorkflowException extends RuntimeException {

    public InvalidWorkflowException(String message) {
        super(message);
    }
}
