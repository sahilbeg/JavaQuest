package jq.m09.resilience;

/** The provider did not answer before our timeout. Given to you. */
public class ProviderTimeoutException extends RuntimeException {

    public ProviderTimeoutException(String message) {
        super(message);
    }
}
