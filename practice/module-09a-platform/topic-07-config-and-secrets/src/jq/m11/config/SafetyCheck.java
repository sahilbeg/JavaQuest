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
        // TODO 13: no "://" in url: return "".
        //          Take the text after "://" and cut it at the first '/' or '?'.
        //          Drop everything up to and including the last '@' (the user and password),
        //          then cut at the first ':' (the port). Return it in lower case (Locale.ROOT).
        return "";
    }

    /** Throws UnsafeConfigException unless spoke.env is known and the database host matches it. */
    public static void check(Map<String, String> config) {
        // TODO 14: spoke.env missing or not in ENVIRONMENTS: throw UnsafeConfigException quoting the value.
        //          spoke.db.url missing or blank: throw UnsafeConfigException.
        // TODO 15: host = hostOf(url). Throw UnsafeConfigException, naming spoke.env and the host (never the URL,
        //          which may hold the password), when
        //          - spoke.env is not prod, but host contains "prod", or
        //          - spoke.env is prod, but host does not contain "prod".
    }
}
