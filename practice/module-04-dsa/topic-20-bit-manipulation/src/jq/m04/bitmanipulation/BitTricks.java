package jq.m04.bitmanipulation;

/** Small bit tricks for the dock sensors. Each method is O(1) or close to it. */
public final class BitTricks {

    private BitTricks() {
    }

    /** True when bit number bit (0 is the lowest, 31 the sign bit) is 1. Throws for a bit outside 0 to 31. */
    public static boolean isBitSet(int value, int bit) {
        // TODO 6: throw IllegalArgumentException when bit is below 0 or above 31.
        // Then shift the bit down to position 0 with >>> and check it with & 1.
        return false;
    }

    /** How many bits are 1, in the int's 32-bit two's complement form. Works for negatives too. */
    public static int countSetBits(int value) {
        // TODO 7: while value is not 0, clear its lowest 1 bit with value & (value - 1) and count.
        return 0;
    }

    /** True for 1, 2, 4, 8, ... Never true for 0 or a negative number. */
    public static boolean isPowerOfTwo(int value) {
        // TODO 8: a power of two is positive and has exactly one 1 bit.
        return false;
    }

    /** Every dock id appears twice except one. Returns that one. */
    public static int unpairedDock(int[] dockIds) {
        // TODO 9: XOR all the ids together. Pairs cancel out, because a ^ a == 0.
        return 0;
    }

    /** A 64-dock station: bit d of the result is 1 when dock d is occupied. Docks are 0 to 63. */
    public static long occupiedMask(int... docks) {
        // TODO 10: throw IllegalArgumentException for a dock outside 0 to 63.
        // Turn on bit d for each dock d. Shift a long (1L), not an int.
        return 0L;
    }

    public static void main(String[] args) {
        System.out.println("Bits set in 2563: " + countSetBits(2563));
        System.out.println("Is 64 a power of two? " + isPowerOfTwo(64));
        System.out.println("Unpaired dock: " + unpairedDock(new int[] {12, 7, 40, 7, 12}));
        System.out.println("Docks 0, 3 and 40: " + Long.toBinaryString(occupiedMask(0, 3, 40)));
    }
}
