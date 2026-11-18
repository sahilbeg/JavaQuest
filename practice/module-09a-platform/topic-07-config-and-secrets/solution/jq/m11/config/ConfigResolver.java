package jq.m11.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Merges Spoke's configuration sources, resolves ${...} placeholders and checks required keys.
 * Priority, lowest first: defaults, application file, profile file, environment variables, command line.
 */
public final class ConfigResolver {

    /** Only environment variables whose name starts with this are Spoke configuration. */
    public static final String ENV_PREFIX = "SPOKE_";

    private ConfigResolver() {
    }

    /** SPOKE_DB_URL becomes spoke.db.url. A name without the SPOKE_ prefix, such as PATH, is not Spoke config. */
    public static Optional<String> envNameToKey(String envName) {
        if (!envName.startsWith(ENV_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(envName.toLowerCase(Locale.ROOT).replace('_', '.'));
    }

    /** All sources in one sorted map, each source overriding the ones before it. Placeholders are left as they are. */
    public static Map<String, String> merge(ConfigSources sources) {
        Map<String, String> merged = new TreeMap<>();
        merged.putAll(sources.defaults());
        merged.putAll(sources.applicationFile());
        merged.putAll(sources.profileFile());

        for (Map.Entry<String, String> variable : sources.environment().entrySet()) {
            Optional<String> key = envNameToKey(variable.getKey());
            if (key.isEmpty()) {
                continue;
            }
            List<String> matches = new ArrayList<>();
            for (String existing : merged.keySet()) {
                if (relaxed(existing).equals(key.get())) {
                    matches.add(existing);
                }
            }
            if (matches.isEmpty()) {
                merged.put(key.get(), variable.getValue());
            } else {
                for (String existing : matches) {
                    merged.put(existing, variable.getValue());
                }
            }
        }

        for (String arg : sources.commandLine()) {
            int equals = arg.indexOf('=');
            if (!arg.startsWith("--") || equals <= 2) {
                throw new IllegalArgumentException("Bad command-line argument: " + arg + " (expected --key=value)");
            }
            merged.put(arg.substring(2, equals), arg.substring(equals + 1));
        }
        return merged;
    }

    /** merge, then replace every ${key} and ${key:default} in every value. Keys are resolved in sorted order. */
    public static Map<String, String> resolve(ConfigSources sources) {
        Map<String, String> raw = merge(sources);
        Map<String, String> resolved = new TreeMap<>();
        for (String key : raw.keySet()) {
            resolved.put(key, resolveKey(key, raw, new ArrayList<>()));
        }
        return resolved;
    }

    /** Throws MissingConfigException naming every required key that is absent or blank, in the order required. */
    public static void requireKeys(Map<String, String> config, List<String> required) {
        List<String> missing = new ArrayList<>();
        for (String key : required) {
            String value = config.get(key);
            if (value == null || value.isBlank()) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            throw new MissingConfigException(missing);
        }
    }

    /** spoke.db.Pool-Size becomes spoke.db.poolsize: lower case, dashes removed. */
    private static String relaxed(String key) {
        return key.replace("-", "").toLowerCase(Locale.ROOT);
    }

    /** The fully resolved value of key. path holds the keys being resolved right now, to spot a cycle. */
    private static String resolveKey(String key, Map<String, String> raw, List<String> path) {
        if (path.contains(key)) {
            path.add(key);
            throw new ConfigException("Placeholder cycle: " + String.join(" -> ", path));
        }
        path.add(key);
        String text = raw.get(key);
        StringBuilder out = new StringBuilder();
        int from = 0;
        while (true) {
            int start = text.indexOf("${", from);
            if (start < 0) {
                out.append(text, from, text.length());
                break;
            }
            int end = text.indexOf('}', start);
            if (end < 0) {
                throw new ConfigException("Unclosed placeholder in " + key + ": " + text);
            }
            out.append(text, from, start);
            String inside = text.substring(start + 2, end);
            int colon = inside.indexOf(':');
            String name = colon < 0 ? inside : inside.substring(0, colon);
            if (raw.containsKey(name)) {
                out.append(resolveKey(name, raw, path));
            } else if (colon >= 0) {
                out.append(inside.substring(colon + 1));
            } else {
                throw new ConfigException("Could not resolve placeholder ${" + name + "} in " + key);
            }
            from = end + 1;
        }
        path.remove(path.size() - 1);
        return out.toString();
    }
}
