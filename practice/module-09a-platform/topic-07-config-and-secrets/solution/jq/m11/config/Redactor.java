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

    /** "://", then a user name, ":", the password, "@". Group 2 is the password. */
    private static final Pattern URL_PASSWORD = Pattern.compile("(://[^:/@]+:)([^@/]+)(@)");

    private Redactor() {
    }

    /** True when the key, in lower case, ends with password, secret, token or key, or contains credentials. */
    public static boolean isSecretKey(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.contains("credentials")) {
            return true;
        }
        for (String ending : SECRET_ENDINGS) {
            if (lower.endsWith(ending)) {
                return true;
            }
        }
        return false;
    }

    /** jdbc:postgresql://spoke_api:hunter2@db/spoke becomes jdbc:postgresql://spoke_api:******@db/spoke. */
    public static String maskUrlPassword(String value) {
        return URL_PASSWORD.matcher(value).replaceAll("$1" + MASK + "$3");
    }

    /** A new sorted map: secret keys get MASK, other values get their URL password masked. The input is not changed. */
    public static Map<String, String> redact(Map<String, String> config) {
        Map<String, String> safe = new TreeMap<>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String value = isSecretKey(entry.getKey()) ? MASK : maskUrlPassword(entry.getValue());
            safe.put(entry.getKey(), value);
        }
        return safe;
    }
}
