package jq.m11.config;

import java.util.List;

/** Thrown when required keys are missing or blank. It lists every one of them. Given to you. */
public class MissingConfigException extends ConfigException {

    private final List<String> missingKeys;

    public MissingConfigException(List<String> missingKeys) {
        super("Missing required config: " + String.join(", ", missingKeys));
        this.missingKeys = List.copyOf(missingKeys);
    }

    /** The missing keys, in the order they were required. */
    public List<String> missingKeys() {
        return missingKeys;
    }
}
