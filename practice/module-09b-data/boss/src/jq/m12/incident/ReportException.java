package jq.m12.incident;

/** Wraps an SQLException, so the report methods do not have to declare it. */
public class ReportException extends RuntimeException {
    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
