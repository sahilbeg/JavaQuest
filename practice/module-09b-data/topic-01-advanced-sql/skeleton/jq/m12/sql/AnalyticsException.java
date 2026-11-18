package jq.m12.sql;

/** Thrown when one of the report queries cannot run, with a message that names the query and the problem. */
public class AnalyticsException extends RuntimeException {

    public AnalyticsException(String message) {
        super(message);
    }

    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}
