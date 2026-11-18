package jq.m11.config;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Refuses to start when the environment and the database do not belong together. */
public final class SafetyCheck {

    public static final Set<String> ENVIRONMENTS = Set.of("dev", "staging", "prod");

    private SafetyCheck() {
    }

    /**
     * The host of a database URL, in lower case.
     * jdbc:postgresql://spoke_api:pw@db-prod.spoke.internal:5432/spoke gives db-prod.spoke.internal.
     * A URL without "://", such as jdbc:h2:mem:spoke, has no host: "".
     */
    public static String hostOf(String url) {
        int scheme = url.indexOf("://");
        if (scheme < 0) {
            return "";
        }
        String rest = url.substring(scheme + 3);
        int end = rest.length();
        for (char stop : new char[] {'/', '?'}) {
            int at = rest.indexOf(stop);
            if (at >= 0 && at < end) {
                end = at;
            }
        }
        String authority = rest.substring(0, end);
        String hostAndPort = authority.substring(authority.lastIndexOf('@') + 1);
        int colon = hostAndPort.indexOf(':');
        String host = colon < 0 ? hostAndPort : hostAndPort.substring(0, colon);
        return host.toLowerCase(Locale.ROOT);
    }

    /** Throws UnsafeConfigException unless spoke.env is known and the database host matches it. */
    public static void check(Map<String, String> config) {
        String env = config.get("spoke.env");
        if (env == null || !ENVIRONMENTS.contains(env)) {
            throw new UnsafeConfigException("spoke.env must be dev, staging or prod, but was " + env);
        }
        String url = config.get("spoke.db.url");
        if (url == null || url.isBlank()) {
            throw new UnsafeConfigException("spoke.db.url is missing");
        }
        String host = hostOf(url);
        boolean productionHost = host.contains("prod");
        if (!env.equals("prod") && productionHost) {
            throw new UnsafeConfigException("Refusing to start: spoke.env is " + env
                    + ", but the database host " + host + " looks like production");
        }
        if (env.equals("prod") && !productionHost) {
            throw new UnsafeConfigException("Refusing to start: spoke.env is prod, but the database host '"
                    + host + "' is not a production host");
        }
    }
}
