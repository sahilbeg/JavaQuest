package jq.m00.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KioskScreenBronzeTest {

    @Test
    @DisplayName("Bronze: welcome() returns exactly Welcome to Spoke!")
    void welcomeText() {
        assertEquals("Welcome to Spoke!", KioskScreen.welcome(),
                "welcome() should return \"Welcome to Spoke!\" with a capital W, a capital S and an !");
    }
}
