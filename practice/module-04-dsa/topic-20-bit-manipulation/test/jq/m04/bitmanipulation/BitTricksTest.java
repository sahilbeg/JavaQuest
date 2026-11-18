package jq.m04.bitmanipulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BitTricksTest {

    @Test
    @DisplayName("isBitSet reads single bits, including the sign bit of a negative number")
    void isBitSetReadsBits() {
        // 10 is binary 1010: bits 1 and 3 are set.
        assertTrue(BitTricks.isBitSet(10, 1), "isBitSet(10, 1) should be true: 10 is 1010 in binary");
        assertTrue(BitTricks.isBitSet(10, 3), "isBitSet(10, 3) should be true: 10 is 1010 in binary");
        assertFalse(BitTricks.isBitSet(10, 0), "isBitSet(10, 0) should be false: 10 is even, so bit 0 is 0");
        assertFalse(BitTricks.isBitSet(10, 2), "isBitSet(10, 2) should be false: 10 is 1010 in binary");
        assertTrue(BitTricks.isBitSet(-1, 31), "isBitSet(-1, 31) should be true: -1 has all 32 bits set");
        assertTrue(BitTricks.isBitSet(Integer.MIN_VALUE, 31), "Integer.MIN_VALUE has only bit 31, the sign bit, set");
        assertFalse(BitTricks.isBitSet(Integer.MAX_VALUE, 31), "Integer.MAX_VALUE is positive, so bit 31 is 0");
    }

    @Test
    @DisplayName("isBitSet refuses bit numbers outside 0 to 31, because Java wraps the shift distance")
    void isBitSetRejectsBadBits() {
        assertTrue(BitTricks.isBitSet(1, 0), "isBitSet(1, 0) should be true");
        assertThrows(IllegalArgumentException.class, () -> BitTricks.isBitSet(1, 32),
                "isBitSet(1, 32) should throw IllegalArgumentException. An int has bits 0 to 31, "
                        + "and 1 << 32 quietly wraps around to 1");
        assertThrows(IllegalArgumentException.class, () -> BitTricks.isBitSet(1, -1),
                "isBitSet(1, -1) should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("countSetBits counts the 1 bits of zero and positive numbers")
    void countPositive() {
        assertEquals(0, BitTricks.countSetBits(0), "0 has no bits set");
        assertEquals(1, BitTricks.countSetBits(1), "1 is binary 1: one bit");
        assertEquals(2, BitTricks.countSetBits(10), "10 is binary 1010: two bits");
        assertEquals(4, BitTricks.countSetBits(2563), "2563 is binary 101000000011: four bits");
        assertEquals(31, BitTricks.countSetBits(Integer.MAX_VALUE), "Integer.MAX_VALUE is 31 ones");
    }

    @Test
    @DisplayName("countSetBits works on negative numbers and does not loop forever")
    void countNegative() {
        int[] counts = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> new int[] {BitTricks.countSetBits(-1), BitTricks.countSetBits(Integer.MIN_VALUE), BitTricks.countSetBits(-8)},
                "countSetBits never finished on a negative number. Shifting with >> copies the sign bit, so the value "
                        + "never reaches 0. Use value & (value - 1), or >>>");
        assertEquals(32, counts[0], "-1 is 32 ones in two's complement, so countSetBits(-1) should be 32");
        assertEquals(1, counts[1], "Integer.MIN_VALUE is a 1 followed by 31 zeros: one bit");
        assertEquals(29, counts[2], "-8 is 29 ones followed by 000: 29 bits");
    }

    @Test
    @DisplayName("isPowerOfTwo is true for 1, 2, 4, ... and false for 0, negatives and other numbers")
    void powerOfTwo() {
        assertTrue(BitTricks.isPowerOfTwo(1), "1 is 2 to the power 0");
        assertTrue(BitTricks.isPowerOfTwo(64), "64 is 2 to the power 6");
        assertTrue(BitTricks.isPowerOfTwo(1 << 30), "1 << 30 is the largest power of two an int can hold");
        assertFalse(BitTricks.isPowerOfTwo(12), "12 is binary 1100: two bits, so not a power of two");
        assertFalse(BitTricks.isPowerOfTwo(0), "0 is not a power of two, even though 0 & (0 - 1) is 0. Check value > 0 too");
        assertFalse(BitTricks.isPowerOfTwo(-8), "Negative numbers are never powers of two");
        assertFalse(BitTricks.isPowerOfTwo(Integer.MIN_VALUE),
                "Integer.MIN_VALUE has a single 1 bit, but it is negative, so it is not a power of two");
    }

    @Test
    @DisplayName("unpairedDock finds the one id without a partner, including 0 and negatives")
    void unpaired() {
        assertEquals(40, BitTricks.unpairedDock(new int[] {12, 7, 40, 7, 12}),
                "12 and 7 appear twice and cancel out with XOR, so 40 is left");
        assertEquals(1042, BitTricks.unpairedDock(new int[] {1042}), "With one id, that id is the unpaired one");
        assertEquals(-3, BitTricks.unpairedDock(new int[] {5, -3, 5}), "XOR works on negative numbers too: -3 is left");
        assertEquals(0, BitTricks.unpairedDock(new int[] {9, 0, 9}), "The unpaired id can be 0");
    }

    @Test
    @DisplayName("occupiedMask uses a long, so docks 32 to 63 land on the right bit")
    void occupiedMask() {
        assertEquals(0b1001L, BitTricks.occupiedMask(0, 3), "Docks 0 and 3 give binary 1001, which is 9");
        assertEquals((1L << 40) | (1L << 63), BitTricks.occupiedMask(40, 63),
                "Docks 40 and 63 need bits 40 and 63 of a long. Write 1L << dock: an int 1 << 40 wraps to bit 8");
        assertEquals(0L, BitTricks.occupiedMask(), "No occupied docks give 0");
        assertEquals(1L << 5, BitTricks.occupiedMask(5, 5), "The same dock twice still sets one bit");
        assertThrows(IllegalArgumentException.class, () -> BitTricks.occupiedMask(64),
                "occupiedMask(64) should throw IllegalArgumentException: docks are 0 to 63");
    }
}
