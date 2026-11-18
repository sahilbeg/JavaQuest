package jq.m11.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigResolverTest {

    private static ConfigSources only(Map<String, String> applicationFile) {
        return new ConfigSources(Map.of(), applicationFile, Map.of(), Map.of(), List.of());
    }

    @Test
    @DisplayName("SPOKE_DB_URL becomes spoke.db.url, and variables without the SPOKE_ prefix are not Spoke config")
    void envNamesBecomeKeys() {
        assertEquals(Optional.of("spoke.db.url"), ConfigResolver.envNameToKey("SPOKE_DB_URL"),
                "SPOKE_DB_URL: lower case it and turn every _ into a dot, giving spoke.db.url");
        assertEquals(Optional.of("spoke.db.poolsize"), ConfigResolver.envNameToKey("SPOKE_DB_POOLSIZE"),
                "SPOKE_DB_POOLSIZE should become spoke.db.poolsize");
        assertEquals(Optional.empty(), ConfigResolver.envNameToKey("PATH"),
                "PATH does not start with SPOKE_, so it is not Spoke config: return Optional.empty()");
        assertEquals(Optional.empty(), ConfigResolver.envNameToKey("JAVA_HOME"),
                "JAVA_HOME does not start with SPOKE_, so return Optional.empty()");
    }

    @Test
    @DisplayName("Each source beats the ones before it: defaults < application < profile < environment < command line")
    void precedence() {
        ConfigSources sources = new ConfigSources(
                Map.of("spoke.a", "default", "spoke.b", "default", "spoke.c", "default", "spoke.d", "default", "spoke.e", "default"),
                Map.of("spoke.b", "application", "spoke.c", "application", "spoke.d", "application", "spoke.e", "application"),
                Map.of("spoke.c", "profile", "spoke.d", "profile", "spoke.e", "profile"),
                Map.of("SPOKE_D", "environment", "SPOKE_E", "environment"),
                List.of("--spoke.e=command line"));

        Map<String, String> merged = ConfigResolver.merge(sources);

        assertEquals("default", merged.get("spoke.a"), "spoke.a is only in the defaults, so the default stays");
        assertEquals("application", merged.get("spoke.b"), "spoke.b: the application file beats the defaults");
        assertEquals("profile", merged.get("spoke.c"), "spoke.c: the profile file beats the application file");
        assertEquals("environment", merged.get("spoke.d"), "spoke.d: the environment variable SPOKE_D beats the profile file");
        assertEquals("command line", merged.get("spoke.e"), "spoke.e: the command line beats everything else");
        assertEquals(5, merged.size(), "five keys went in, so the merged map should hold exactly five");
    }

    @Test
    @DisplayName("SPOKE_DB_POOLSIZE overrides spoke.db.pool-size from the file, without adding a second key")
    void envMatchesDashedKey() {
        ConfigSources sources = new ConfigSources(
                Map.of(),
                Map.of("spoke.db.pool-size", "10", "spoke.db.url", "jdbc:postgresql://db.staging.spoke.internal/spoke"),
                Map.of(),
                Map.of("SPOKE_DB_POOLSIZE", "20", "SPOKE_CACHE_TTL", "60s"),
                List.of());

        Map<String, String> merged = ConfigResolver.merge(sources);

        assertEquals("20", merged.get("spoke.db.pool-size"),
                "spoke.db.pool-size without its dash is spoke.db.poolsize, which is what SPOKE_DB_POOLSIZE means, so 20 wins");
        assertFalse(merged.containsKey("spoke.db.poolsize"),
                "the variable matched an existing key, so it must overwrite that key, not add spoke.db.poolsize as well");
        assertEquals("60s", merged.get("spoke.cache.ttl"),
                "SPOKE_CACHE_TTL matches no existing key, so it is added as spoke.cache.ttl");
        assertEquals(3, merged.size(), "two keys from the file plus one new key from the environment make three");
    }

    @Test
    @DisplayName("Environment variables without the SPOKE_ prefix never reach the configuration")
    void otherEnvIgnored() {
        ConfigSources sources = new ConfigSources(
                Map.of(), Map.of("spoke.env", "dev"), Map.of(),
                Map.of("PATH", "/usr/bin", "HOME", "/home/spoke", "SPOKE_ENV", "staging"),
                List.of());

        Map<String, String> merged = ConfigResolver.merge(sources);

        assertEquals(Map.of("spoke.env", "staging"), merged,
                "only SPOKE_ENV is Spoke config; PATH and HOME must be skipped, not added as path and home");
    }

    @Test
    @DisplayName("Command-line arguments split at the first =, and anything not shaped --key=value is rejected")
    void commandLineArguments() {
        ConfigSources sources = new ConfigSources(Map.of(), Map.of(), Map.of(), Map.of(),
                List.of("--spoke.db.url=jdbc:postgresql://db.staging.spoke.internal/spoke?ssl=true", "--spoke.banner="));

        Map<String, String> merged = ConfigResolver.merge(sources);

        assertEquals("jdbc:postgresql://db.staging.spoke.internal/spoke?ssl=true", merged.get("spoke.db.url"),
                "split at the FIRST =, so the value keeps its own ?ssl=true");
        assertEquals("", merged.get("spoke.banner"), "--spoke.banner= sets spoke.banner to the empty string");

        for (String bad : List.of("spoke.env=prod", "--spoke.env", "--=prod")) {
            ConfigSources badSources = new ConfigSources(Map.of(), Map.of(), Map.of(), Map.of(), List.of(bad));
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> ConfigResolver.merge(badSources),
                    "'" + bad + "' is not --key=value, so merge should throw IllegalArgumentException");
            assertTrue(e.getMessage().contains(bad), "the message should quote the bad argument '" + bad + "', but was: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("${key} is replaced by that key's value, also inside longer text and through chains of placeholders")
    void placeholders() {
        Map<String, String> resolved = ConfigResolver.resolve(only(Map.of(
                "spoke.db.host", "${spoke.env}-db.spoke.internal",
                "spoke.env", "staging",
                "spoke.db.port", "5432",
                "spoke.db.url", "jdbc:postgresql://${spoke.db.host}:${spoke.db.port}/spoke")));

        assertEquals("staging-db.spoke.internal", resolved.get("spoke.db.host"), "spoke.db.host uses ${spoke.env}, which is staging");
        assertEquals("jdbc:postgresql://staging-db.spoke.internal:5432/spoke", resolved.get("spoke.db.url"),
                "spoke.db.url has two placeholders, and spoke.db.host has its own placeholder inside: resolve them all");
        assertEquals("5432", resolved.get("spoke.db.port"), "a value with no placeholder stays as it is");
    }

    @Test
    @DisplayName("${key:default} uses the default only when the key is missing, and the default is everything after the first colon")
    void defaults() {
        Map<String, String> resolved = ConfigResolver.resolve(only(Map.of(
                "spoke.region", "eu-west",
                "a", "${spoke.region:us-east}",
                "b", "${spoke.zone:zone-1}",
                "c", "${spoke.db.url:jdbc:h2:mem:spoke}",
                "d", "[${spoke.banner:}]")));

        assertEquals("eu-west", resolved.get("a"), "spoke.region exists, so its value wins over the default us-east");
        assertEquals("zone-1", resolved.get("b"), "spoke.zone is missing, so the default zone-1 is used");
        assertEquals("jdbc:h2:mem:spoke", resolved.get("c"), "split at the FIRST colon: the default is jdbc:h2:mem:spoke");
        assertEquals("[]", resolved.get("d"), "${spoke.banner:} has an empty default, so nothing goes between the brackets");
    }

    @Test
    @DisplayName("Placeholders see the merged value, so an environment variable reaches every value that uses its key")
    void placeholdersAfterMerge() {
        ConfigSources sources = new ConfigSources(
                Map.of(),
                Map.of("spoke.db.host", "localhost", "spoke.db.url", "jdbc:postgresql://${spoke.db.host}/spoke"),
                Map.of(),
                Map.of("SPOKE_DB_HOST", "db.staging.spoke.internal"),
                List.of());

        assertEquals("jdbc:postgresql://db.staging.spoke.internal/spoke", ConfigResolver.resolve(sources).get("spoke.db.url"),
                "merge every source first, then resolve: the placeholder must use the environment's host, not the file's localhost");
    }

    @Test
    @DisplayName("A placeholder with no value and no default fails with a message naming it and the key that used it")
    void unresolvable() {
        ConfigException e = assertThrows(ConfigException.class,
                () -> ConfigResolver.resolve(only(Map.of("spoke.db.url", "jdbc:postgresql://${spoke.db.host}/spoke"))),
                "spoke.db.host does not exist and there is no default, so resolve must throw ConfigException");
        assertEquals("Could not resolve placeholder ${spoke.db.host} in spoke.db.url", e.getMessage(),
                "the message should say which placeholder and which key");
    }

    @Test
    @DisplayName("Placeholders that point at each other fail with the whole cycle, instead of looping forever")
    void cycles() {
        ConfigException e = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> assertThrows(ConfigException.class,
                () -> ConfigResolver.resolve(only(Map.of("spoke.a", "${spoke.b}", "spoke.b", "x-${spoke.c}", "spoke.c", "${spoke.a}"))),
                "a -> b -> c -> a is a cycle, so resolve must throw ConfigException"),
                "resolve must notice the cycle, not loop until the stack overflows");
        assertEquals("Placeholder cycle: spoke.a -> spoke.b -> spoke.c -> spoke.a", e.getMessage(),
                "resolve keys in sorted order and report the path from the first key back to itself");

        ConfigException self = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> assertThrows(ConfigException.class,
                () -> ConfigResolver.resolve(only(Map.of("spoke.city", "${spoke.city}")))),
                "a key that uses itself is a cycle too");
        assertEquals("Placeholder cycle: spoke.city -> spoke.city", self.getMessage(), "a self-reference is the shortest cycle");
    }

    @Test
    @DisplayName("A key used twice is not a cycle: two placeholders may point at the same key")
    void reuseIsNotACycle() {
        Map<String, String> resolved = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> ConfigResolver.resolve(only(Map.of(
                "spoke.env", "staging",
                "spoke.label", "${spoke.env}/${spoke.env}"))));

        assertEquals("staging/staging", resolved.get("spoke.label"),
                "spoke.env is used twice in one value, which is fine. Only remove a key from the path once it is resolved.");
    }

    @Test
    @DisplayName("requireKeys lists every missing or blank key at once, in the order required")
    void requiredKeys() {
        Map<String, String> config = Map.of("spoke.env", "staging", "spoke.db.username", "spoke_api", "spoke.db.password", "  ");

        MissingConfigException e = assertThrows(MissingConfigException.class,
                () -> ConfigResolver.requireKeys(config, List.of("spoke.db.url", "spoke.env", "spoke.db.password", "spoke.payment.api-key")),
                "three required keys are missing or blank, so requireKeys must throw MissingConfigException");
        assertEquals(List.of("spoke.db.url", "spoke.db.password", "spoke.payment.api-key"), e.missingKeys(),
                "report ALL three problems in one go, in the order required. A password of only spaces counts as missing.");

        ConfigResolver.requireKeys(config, List.of("spoke.env", "spoke.db.username"));
    }
}
