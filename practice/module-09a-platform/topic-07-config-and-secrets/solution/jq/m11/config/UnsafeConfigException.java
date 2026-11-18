package jq.m11.config;

/** Thrown when the configuration looks dangerous, such as staging pointing at production. Given to you. */
public class UnsafeConfigException extends ConfigException {

    public UnsafeConfigException(String message) {
        super(message);
    }
}
