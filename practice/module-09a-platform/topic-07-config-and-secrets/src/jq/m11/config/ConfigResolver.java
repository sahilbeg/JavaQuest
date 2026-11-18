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
        // TODO 1: names that do not start with ENV_PREFIX: return Optional.empty().
        //         Otherwise lower-case the whole name (Locale.ROOT) and replace every '_' with '.'.
        return Optional.empty();
    }

    /** All sources in one sorted map, each source overriding the ones before it. Placeholders are left as they are. */
    public static Map<String, String> merge(ConfigSources sources) {
        Map<String, String> merged = new TreeMap<>();
        // TODO 2: putAll the defaults, then the application file, then the profile file (later ones overwrite).
        // TODO 3: for each environment variable whose envNameToKey is present:
        //         find every key already in merged whose relaxed(key) equals that env key, and overwrite each.
        //         No key matches? put the env key itself. (Collect the matches first: never change a map while looping over it.)
        // TODO 4: for each command-line argument: it must start with "--" and have an '=' after a non-empty key,
        //         or throw IllegalArgumentException with the argument in the message.
        //         Split at the FIRST '=': key = the text between "--" and '=', value = everything after it.
        return merged;
    }

    /** merge, then replace every ${key} and ${key:default} in every value. Keys are resolved in sorted order. */
    public static Map<String, String> resolve(ConfigSources sources) {
        Map<String, String> raw = merge(sources);
        Map<String, String> resolved = new TreeMap<>();
        // TODO 5: for each key of raw (a TreeMap, so sorted), put resolveKey(key, raw, new ArrayList<>()) into resolved.
        return resolved;
    }

    /** Throws MissingConfigException naming every required key that is absent or blank, in the order required. */
    public static void requireKeys(Map<String, String> config, List<String> required) {
        // TODO 6: collect every required key whose value is null or blank (isBlank()), keeping the order of required.
        //         If any are missing, throw new MissingConfigException(thatList). Do not stop at the first one.
    }

    /** spoke.db.Pool-Size becomes spoke.db.poolsize: lower case, dashes removed. Given to you. */
    private static String relaxed(String key) {
        return key.replace("-", "").toLowerCase(Locale.ROOT);
    }

    /** The fully resolved value of key. path holds the keys being resolved right now, to spot a cycle. */
    private static String resolveKey(String key, Map<String, String> raw, List<String> path) {
        // TODO 7: if path already contains key, add key to path and throw
        //         new ConfigException("Placeholder cycle: " + String.join(" -> ", path)).
        //         Otherwise add key to path.
        String text = raw.get(key);
        StringBuilder out = new StringBuilder();
        // TODO 8: walk through text. Copy plain text to out. For each "${...}":
        //         - no closing '}': throw new ConfigException("Unclosed placeholder in " + key + ": " + text)
        //         - split the inside at its FIRST ':' into name and default (no ':' means no default)
        //         - name is in raw: append resolveKey(name, raw, path)
        //         - else a default exists: append the default text (it may be empty)
        //         - else throw new ConfigException("Could not resolve placeholder ${" + name + "} in " + key)
        // TODO 9: remove key from the end of path (it is resolved now), then return out.toString().
        return text;
    }
}
