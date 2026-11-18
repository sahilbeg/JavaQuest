package jq.m12.feature;

/** The nightly statement job failed. Nothing it did in that run was kept. */
public class StatementJobException extends RuntimeException {
    public StatementJobException(String message, Throwable cause) {
        super(message, cause);
    }
}
