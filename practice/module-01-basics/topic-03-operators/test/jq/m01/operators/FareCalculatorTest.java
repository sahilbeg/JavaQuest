package jq.m01.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FareCalculatorTest {

    @Test
    @DisplayName("classicFare charges the unlock fee plus 0.15 a minute")
    void classicFare() {
        assertEquals(2.5, FareCalculator.classicFare(10), 0.001,
                "classicFare(10) should be 2.5: unlock fee 1.00 plus 10 minutes at 0.15");
        assertEquals(5.5, FareCalculator.classicFare(30), 0.001,
                "classicFare(30) should be 5.5: unlock fee 1.00 plus 30 minutes at 0.15");
        assertEquals(1.0, FareCalculator.classicFare(0), 0.001,
                "classicFare(0) should be 1.0: a rider always pays the 1.00 unlock fee");
    }

    @Test
    @DisplayName("eBikeFare charges the unlock fee plus 0.30 a minute")
    void eBikeFare() {
        assertEquals(4.0, FareCalculator.eBikeFare(10), 0.001,
                "eBikeFare(10) should be 4.0: unlock fee 1.00 plus 10 minutes at 0.30");
        assertEquals(10.0, FareCalculator.eBikeFare(30), 0.001,
                "eBikeFare(30) should be 10.0: unlock fee 1.00 plus 30 minutes at 0.30");
    }

    @Test
    @DisplayName("hoursAndMinutes splits minutes into hours and leftover minutes")
    void hoursAndMinutes() {
        assertEquals("1 h 15 min", FareCalculator.hoursAndMinutes(75),
                "hoursAndMinutes(75) should be \"1 h 15 min\": use minutes / 60 and minutes % 60");
        assertEquals("0 h 45 min", FareCalculator.hoursAndMinutes(45),
                "hoursAndMinutes(45) should be \"0 h 45 min\": less than an hour still shows 0 h");
        assertEquals("2 h 0 min", FareCalculator.hoursAndMinutes(120),
                "hoursAndMinutes(120) should be \"2 h 0 min\": 120 % 60 leaves 0");
    }

    @Test
    @DisplayName("fareLine joins the label and the classic fare")
    void fareLine() {
        assertEquals("Fare: $2.5", FareCalculator.fareLine(10),
                "fareLine(10) should be \"Fare: $2.5\": join \"Fare: $\" with classicFare(10)");
        assertEquals("Fare: $4.0", FareCalculator.fareLine(20),
                "fareLine(20) should be \"Fare: $4.0\": join \"Fare: $\" with classicFare(20)");
    }
}
