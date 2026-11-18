package jq.m11.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedactorTest {

    @Test
    @DisplayName("Keys ending in password, secret, token or key, or containing credentials, are secret, in any case")
    void secretKeys() {
        for (String key : List.of("spoke.db.password", "spoke.jwt.signing-secret", "spoke.github.token",
                "spoke.payment.api-key", "spoke.aws.credentials.id", "SPOKE_DB_PASSWORD", "spoke.maps.apiKey")) {
            assertTrue(Redactor.isSecretKey(key), key + " should count as secret");
        }
        for (String key : List.of("spoke.db.url", "spoke.db.username", "spoke.cache.key-prefix", "spoke.rate.tokens-per-minute")) {
            assertFalse(Redactor.isSecretKey(key), key + " should not count as secret: it does not END with a secret word");
        }
    }

    @Test
    @DisplayName("redact masks secret values with six stars and leaves the rest readable")
    void redactsSecrets() {
        Map<String, String> safe = Redactor.redact(Map.of(
                "spoke.db.password", "hunter2",
                "spoke.payment.api-key", "pay-key-live-7",
                "spoke.env", "staging",
                "spoke.db.pool-size", "20"));

        assertEquals("******", safe.get("spoke.db.password"), "the password must be replaced by exactly six stars");
        assertEquals("******", safe.get("spoke.payment.api-key"), "the API key must be replaced by six stars, whatever its length");
        assertEquals("staging", safe.get("spoke.env"), "spoke.env is not secret, so it stays readable");
        assertEquals("20", safe.get("spoke.db.pool-size"), "the pool size is not secret, so it stays readable");
        assertEquals(4, safe.size(), "every key stays in the output; only values change");
    }

    @Test
    @DisplayName("A password hidden inside a URL is masked too, and the user name stays")
    void masksPasswordInUrl() {
        Map<String, String> safe = Redactor.redact(Map.of(
                "spoke.db.url", "jdbc:postgresql://spoke_api:hunter2@db.staging.spoke.internal:5432/spoke",
                "spoke.cache.url", "redis://cache.staging.spoke.internal:6379"));

        assertEquals("jdbc:postgresql://spoke_api:******@db.staging.spoke.internal:5432/spoke", safe.get("spoke.db.url"),
                "spoke.db.url is not a secret key, but its value holds user:password@: mask the password part");
        assertEquals("redis://cache.staging.spoke.internal:6379", safe.get("spoke.cache.url"),
                "a URL without user:password@ must stay exactly as it is, port and all");
    }

    @Test
    @DisplayName("redact returns a new sorted map and never changes the real configuration")
    void doesNotChangeInput() {
        Map<String, String> config = new HashMap<>(Map.of("spoke.env", "dev", "spoke.db.password", "hunter2", "spoke.city", "Riverton"));

        Map<String, String> safe = Redactor.redact(config);

        assertEquals("hunter2", config.get("spoke.db.password"),
                "the app still needs the real password: redact must build a new map, not mask the one it was given");
        assertEquals(List.of("spoke.city", "spoke.db.password", "spoke.env"), List.copyOf(safe.keySet()),
                "the redacted map should be sorted by key, so the printed config is easy to scan");
    }
}
