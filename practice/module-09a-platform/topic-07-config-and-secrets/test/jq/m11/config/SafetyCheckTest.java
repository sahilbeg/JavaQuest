package jq.m11.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SafetyCheckTest {

    @Test
    @DisplayName("hostOf finds the host behind the user, password and port, in lower case")
    void hosts() {
        assertEquals("db-prod.spoke.internal", SafetyCheck.hostOf("jdbc:postgresql://spoke_api:hunter2@DB-PROD.spoke.internal:5432/spoke"),
                "skip spoke_api:hunter2@, stop at the :5432 port, and lower-case the host");
        assertEquals("db.staging.spoke.internal", SafetyCheck.hostOf("jdbc:postgresql://db.staging.spoke.internal/spoke?ssl=true"),
                "with no user and no port, the host ends at the first /");
        assertEquals("localhost", SafetyCheck.hostOf("jdbc:postgresql://localhost:5432/spoke"), "localhost is a host too");
        assertEquals("", SafetyCheck.hostOf("jdbc:h2:mem:spoke"), "a URL without :// has no host, so hostOf returns \"\"");
    }

    @Test
    @DisplayName("Staging pointing at a production database refuses to start, and the message never shows the password")
    void stagingOnProdDatabase() {
        UnsafeConfigException e = assertThrows(UnsafeConfigException.class, () -> SafetyCheck.check(Map.of(
                        "spoke.env", "staging",
                        "spoke.db.url", "jdbc:postgresql://spoke_api:hunter2@db-prod.spoke.internal:5432/spoke")),
                "spoke.env is staging but the host contains prod: check must throw UnsafeConfigException");
        assertTrue(e.getMessage().contains("staging") && e.getMessage().contains("db-prod.spoke.internal"),
                "the message should name the environment and the host, but was: " + e.getMessage());
        assertFalse(e.getMessage().contains("hunter2"), "an error message must never contain the password, but was: " + e.getMessage());

        assertThrows(UnsafeConfigException.class, () -> SafetyCheck.check(Map.of(
                        "spoke.env", "dev", "spoke.db.url", "jdbc:postgresql://replica.prod.spoke.internal/spoke")),
                "dev pointing at a prod replica is just as dangerous: refuse it");
    }

    @Test
    @DisplayName("Production pointing at a database whose host does not contain prod refuses to start")
    void prodOnOtherDatabase() {
        UnsafeConfigException e = assertThrows(UnsafeConfigException.class, () -> SafetyCheck.check(Map.of(
                        "spoke.env", "prod", "spoke.db.url", "jdbc:postgresql://db.staging.spoke.internal:5432/spoke")),
                "spoke.env is prod but the host has no prod in it: check must throw UnsafeConfigException");
        assertTrue(e.getMessage().contains("prod") && e.getMessage().contains("db.staging.spoke.internal"),
                "the message should name the environment and the host, but was: " + e.getMessage());

        assertThrows(UnsafeConfigException.class, () -> SafetyCheck.check(Map.of(
                        "spoke.env", "prod", "spoke.db.url", "jdbc:h2:mem:spoke")),
                "production on an in-memory database has no host at all: refuse it");
    }

    @Test
    @DisplayName("Matching pairs start normally")
    void matchingPairsPass() {
        assertDoesNotThrow(() -> SafetyCheck.check(Map.of("spoke.env", "prod",
                "spoke.db.url", "jdbc:postgresql://spoke_api:hunter2@db-prod.spoke.internal:5432/spoke")),
                "prod with a prod host is the normal case and must start");
        assertDoesNotThrow(() -> SafetyCheck.check(Map.of("spoke.env", "staging",
                "spoke.db.url", "jdbc:postgresql://db.staging.spoke.internal/spoke")), "staging with a staging host must start");
        assertDoesNotThrow(() -> SafetyCheck.check(Map.of("spoke.env", "dev", "spoke.db.url", "jdbc:h2:mem:spoke")),
                "dev on an in-memory database must start");
        assertEquals("db-prod.spoke.internal", SafetyCheck.hostOf("jdbc:postgresql://db-prod.spoke.internal/spoke"),
                "check relies on hostOf, so hostOf must work too");
    }

    @Test
    @DisplayName("A missing or unknown spoke.env, or a missing database URL, refuses to start")
    void missingOrUnknown() {
        String url = "jdbc:postgresql://db.staging.spoke.internal/spoke";
        assertThrows(UnsafeConfigException.class, () -> SafetyCheck.check(Map.of("spoke.db.url", url)),
                "with no spoke.env we cannot tell where we are: refuse to start");
        UnsafeConfigException e = assertThrows(UnsafeConfigException.class,
                () -> SafetyCheck.check(Map.of("spoke.env", "production", "spoke.db.url", url)),
                "production is not one of dev, staging, prod: refuse to start rather than guess");
        assertTrue(e.getMessage().contains("production"), "the message should quote the unknown value, but was: " + e.getMessage());
        Map<String, String> noUrl = new HashMap<>(Map.of("spoke.env", "staging"));
        assertThrows(UnsafeConfigException.class, () -> SafetyCheck.check(noUrl), "with no spoke.db.url, refuse to start");
    }
}
