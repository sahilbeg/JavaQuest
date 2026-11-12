package jq.m04.recursion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecursionTest {

    @Test
    @DisplayName("factorial multiplies n by every whole number below it")
    void factorial() {
        assertEquals(1L, Recursion.factorial(0), "factorial(0) should be 1: that is the base case");
        assertEquals(1L, Recursion.factorial(1), "factorial(1) should be 1: that is the base case");
        assertEquals(120L, Recursion.factorial(5), "factorial(5) should be 120: 5 * 4 * 3 * 2 * 1");
        assertEquals(6227020800L, Recursion.factorial(13), "factorial(13) should be 6227020800: return a long so big results fit");
    }

    @Test
    @DisplayName("sum adds the free docks along the whole overflow chain")
    void sum() {
        int[] chain = {3, 0, 5, 2};
        assertEquals(10, Recursion.sum(chain), "sum({3, 0, 5, 2}) should be 10: Riverside 3 + Old Mill 0 + Harbor 5 + Uptown 2");
        assertEquals(7, Recursion.sum(new int[] {7}), "sum({7}) should be 7: a chain of one station");
        assertEquals(0, Recursion.sum(new int[0]), "sum of an empty chain should be 0: return 0 when index reaches docks.length");
        assertEquals(12, Recursion.sum(new int[] {0, 0, 0, 12}), "sum({0, 0, 0, 12}) should be 12: do not forget the last station");
    }

    @Test
    @DisplayName("countDown builds the text from n down to go")
    void countDown() {
        assertEquals("3 2 1 go", Recursion.countDown(3), "countDown(3) should be \"3 2 1 go\" with single spaces");
        assertEquals("1 go", Recursion.countDown(1), "countDown(1) should be \"1 go\"");
        assertEquals("go", Recursion.countDown(0), "countDown(0) should be just \"go\": that is the base case");
    }

    @Test
    @DisplayName("reverse turns text back to front")
    void reverse() {
        assertEquals("2401-B", Recursion.reverse("B-1042"), "reverse(\"B-1042\") should be \"2401-B\"");
        assertEquals("a", Recursion.reverse("a"), "reverse(\"a\") should be \"a\"");
        assertEquals("", Recursion.reverse(""), "reverse(\"\") should be \"\": the empty string is the base case");
    }

    @Test
    @DisplayName("power multiplies base by itself exp times")
    void power() {
        assertEquals(1024, Recursion.power(2, 10), "power(2, 10) should be 1024");
        assertEquals(1, Recursion.power(12, 0), "power(12, 0) should be 1: anything to the power 0 is 1");
        assertEquals(144, Recursion.power(12, 2), "power(12, 2) should be 144: 12 * 12");
    }
}
