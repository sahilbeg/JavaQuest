package jq.m09.resilience;

/** The payment or SMS provider answered with an HTTP error status, such as 503 or 409. Given to you. */
public class ProviderException extends RuntimeException {

    private final int status;

    public ProviderException(int status, String message) {
        super(status + " " + message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
