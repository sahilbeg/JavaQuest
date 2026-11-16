package jq.m07.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReleaseGateBronzeTest {

    @Test
    @DisplayName("Bronze: 1.10.0 may ship over 1.9.3")
    void newerReleaseShips() {
        assertTrue(ReleaseGate.canShip("1.9.3", "1.10.0"), "1.10.0 is newer than 1.9.3, because minor version 10 is bigger than 9");
        assertTrue(ReleaseGate.canShip("v1.2.0", "v1.2.10"), "patch 10 is bigger than patch 0");
        assertTrue(ReleaseGate.canShip("1.10.0", "2.0.0"), "major version 2 is bigger than 1");
    }

    @Test
    @DisplayName("Bronze: an older or equal release is blocked")
    void olderReleaseIsBlocked() {
        assertFalse(ReleaseGate.canShip("1.10.0", "1.9.3"), "1.9.3 is older than the live 1.10.0, so it must not ship");
        assertFalse(ReleaseGate.canShip("1.10.0", "1.10.0"), "the same version is already live, so there is nothing new to ship");
    }

    @Test
    @DisplayName("Bronze: versions sort by number, not by text")
    void versionsSortByNumber() {
        List<String> sorted = Stream.of("1.10.0", "2.0.0", "1.2.0", "1.10.1", "1.9.3")
                .map(Version::parse)
                .sorted()
                .map(Version::toString)
                .toList();
        assertEquals(List.of("1.2.0", "1.9.3", "1.10.0", "1.10.1", "2.0.0"), sorted,
                "compare major first, then minor, then patch, each as a whole number");
    }
}
