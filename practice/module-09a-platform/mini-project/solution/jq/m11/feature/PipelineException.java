package jq.m11.feature;

/** Thrown when a pipeline file is not a valid pipeline. The message says what is wrong, and where. */
public class PipelineException extends RuntimeException {

    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String message, Throwable cause) {
        super(message, cause);
    }
}
