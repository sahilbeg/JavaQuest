package jq.m11.config;

import java.util.List;
import java.util.Map;

/**
 * Everything the app can read its configuration from, lowest priority first. Given to you.
 *
 * @param defaults        built-in defaults in the code
 * @param applicationFile the keys from application.yml
 * @param profileFile     the keys from the active profile's file, such as application-staging.yml
 * @param environment     the OS environment variables, with their real names, such as SPOKE_DB_URL
 * @param commandLine     the command-line arguments, such as --spoke.db.pool-size=20
 */
public record ConfigSources(
        Map<String, String> defaults,
        Map<String, String> applicationFile,
        Map<String, String> profileFile,
        Map<String, String> environment,
        List<String> commandLine) {

    public ConfigSources {
        defaults = Map.copyOf(defaults);
        applicationFile = Map.copyOf(applicationFile);
        profileFile = Map.copyOf(profileFile);
        environment = Map.copyOf(environment);
        commandLine = List.copyOf(commandLine);
    }
}
