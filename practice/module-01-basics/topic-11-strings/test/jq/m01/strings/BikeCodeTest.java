package jq.m01.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeCodeTest {

    @Test
    @DisplayName("normalize removes spaces and makes the code upper case")
    void normalizeTrimsAndUppercases() {
        assertEquals("B-1042", BikeCode.normalize(" b-1042 "),
                "normalize(\" b-1042 \") should be \"B-1042\": use trim() and toUpperCase()");
    }

    @Test
    @DisplayName("normalize adds the missing dash after the B")
    void normalizeAddsDash() {
        assertEquals("B-1042", BikeCode.normalize("b1042"),
                "normalize(\"b1042\") should be \"B-1042\": add \"B-\" in front of substring(1)");
        assertEquals("B-1042", BikeCode.normalize("B1042"),
                "normalize(\"B1042\") should be \"B-1042\": the dash goes right after the B");
    }

    @Test
    @DisplayName("isValid accepts B- followed by exactly 4 digits")
    void isValidAcceptsGoodCodes() {
        assertTrue(BikeCode.isValid("B-1042"), "isValid(\"B-1042\") should be true");
        assertTrue(BikeCode.isValid("B-0007"), "isValid(\"B-0007\") should be true: leading zeros are digits too");
    }

    @Test
    @DisplayName("isValid rejects codes with the wrong length, start or characters")
    void isValidRejectsBadCodes() {
        assertFalse(BikeCode.isValid("B-104"), "isValid(\"B-104\") should be false: only 3 digits, so length is 5");
        assertFalse(BikeCode.isValid("B-10422"), "isValid(\"B-10422\") should be false: 5 digits is too many");
        assertFalse(BikeCode.isValid("C-1042"), "isValid(\"C-1042\") should be false: the code must start with \"B-\"");
        assertFalse(BikeCode.isValid("B-10x2"), "isValid(\"B-10x2\") should be false: x is not a digit");
    }

    @Test
    @DisplayName("number gives back the 4 digits as text")
    void numberGivesDigits() {
        assertEquals("1042", BikeCode.number("B-1042"),
                "number(\"B-1042\") should be \"1042\": use substring(2)");
    }
}
