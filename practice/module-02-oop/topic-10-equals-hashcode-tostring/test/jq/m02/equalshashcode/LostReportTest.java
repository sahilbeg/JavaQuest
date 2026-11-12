package jq.m02.equalshashcode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LostReportTest {

    @Test
    @DisplayName("toString shows the id and the type")
    void toStringShowsIdAndType() {
        assertEquals("Bike[B-1042, ebike]", new Bike("B-1042", "ebike").toString(),
                "toString() should return Bike[B-1042, ebike]: the word Bike, then id, a comma and a space, then type, inside [ ]");
    }

    @Test
    @DisplayName("Two bikes with the same id are equal, even as separate objects")
    void sameIdMeansEqual() {
        Bike a = new Bike("B-1042", "ebike");
        Bike b = new Bike("B-1042", "ebike");
        assertTrue(a.equals(b), "two Bike objects with id B-1042 should be equal. Compare the ids with equals");
        assertFalse(a.equals(new Bike("B-2001", "ebike")), "bikes with different ids should not be equal");
        assertFalse(a.equals("B-1042"), "a Bike should never equal a String, even one with the same text");
        assertFalse(a.equals(null), "a Bike should never equal null");
    }

    @Test
    @DisplayName("Equal bikes have the same hash code")
    void equalBikesShareHashCode() {
        Bike a = new Bike("B-1042", "ebike");
        Bike b = new Bike("B-1042", "classic");
        assertEquals(a.hashCode(), b.hashCode(),
                "bikes with the same id must have the same hashCode(). Build it from id only");
    }

    @Test
    @DisplayName("countDistinct counts each bike id once")
    void countDistinctIgnoresDuplicates() {
        Bike[] report = {
            new Bike("B-1042", "ebike"),
            new Bike("B-2001", "classic"),
            new Bike("B-1042", "ebike"),
            new Bike("B-3300", "cargo"),
            new Bike("B-2001", "classic")
        };
        assertEquals(3, LostReport.countDistinct(report),
                "the report lists B-1042 twice and B-2001 twice, so there are 3 different bikes");
        assertEquals(0, LostReport.countDistinct(new Bike[0]), "an empty report has 0 different bikes");
    }

    @Test
    @DisplayName("contains finds a bike by equals, not by ==")
    void containsUsesEquals() {
        Bike[] report = { new Bike("B-1042", "ebike"), new Bike("B-2001", "classic") };
        assertTrue(LostReport.contains(report, new Bike("B-2001", "classic")),
                "contains should find B-2001 even though the target is a new object. Use equals, not ==");
        assertFalse(LostReport.contains(report, new Bike("B-9999", "cargo")),
                "contains should return false for B-9999, which is not in the report");
    }
}
