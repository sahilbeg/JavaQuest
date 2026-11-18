package jq.m11.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/** Masks secrets before the effective configuration is printed or logged. */
public final class Redactor {

    /** What every secret value is replaced with. Always six stars, so even the length stays hidden. */
    public static final String MASK = "******";

    private static final List<String> SECRET_ENDINGS = List.of("password", "secret", "token", "key");

    /** "://", then a user name, ":", the password, "@". Group 2 is the password. Given to you. */
    private static final Pattern URL_PASSWORD = Pattern.compile("(://[^:/@]+:)([^@/]+)(@)");

    private Redactor() {
    }

    /** True when the key, in lower case, ends with password, secret, token or key, or contains credentials. */
    public static boolean isSecretKey(String key) {
        // TODO 10: lower-case the key (Locale.ROOT). Secret if it contains "credentials",
        //          or ends with one of SECRET_ENDINGS.
        return false;
    }

    /** jdbc:postgresql://spoke_api:hunter2@db/spoke becomes jdbc:postgresql://spoke_api:******@db/spoke. */
    public static String maskUrlPassword(String value) {
        // TODO 11: URL_PASSWORD.matcher(value).replaceAll(...) keeping groups 1 and 3 and putting MASK in place of group 2
        return value;
    }

    /** A new sorted map: secret keys get MASK, other values get their URL password masked. The input is not changed. */
    public static Map<String, String> redact(Map<String, String> config) {
        Map<String, String> safe = new TreeMap<>();
        // TODO 12: for every entry, put MASK when isSecretKey(key), otherwise maskUrlPassword(value)
        return safe;
    }
}
