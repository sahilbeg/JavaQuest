package jq.m11.config;

/** Thrown when the configuration cannot be used: the app must not start. Given to you. */
public class ConfigException extends RuntimeException {

    public ConfigException(String message) {
        super(message);
    }
}
