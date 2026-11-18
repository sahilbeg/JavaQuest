package jq.m12.pipelines;

/** Thrown when a DAG cannot be run: no tasks, a duplicate name, an unknown dependency or a cycle. */
public class InvalidDagException extends IllegalArgumentException {
    public InvalidDagException(String message) {
        super(message);
    }
}
