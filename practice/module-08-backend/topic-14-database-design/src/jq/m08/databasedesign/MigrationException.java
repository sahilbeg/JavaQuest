package jq.m08.databasedesign;

/** Thrown when migrations cannot be applied safely. Unchecked, so callers need no throws clause. */
public class MigrationException extends RuntimeException {

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
