package jq.m08.jdbc;

/** An unchecked exception for database trouble. It keeps the original SQLException as its cause. */
public class RideRepositoryException extends RuntimeException {

    public RideRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
