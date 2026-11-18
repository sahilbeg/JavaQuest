package jq.m04.bitmanipulation;

/** Small bit tricks for the dock sensors. Each method is O(1) or close to it. */
public final class BitTricks {

    private BitTricks() {
    }

    /** True when bit number bit (0 is the lowest, 31 the sign bit) is 1. Throws for a bit outside 0 to 31. */
    public static boolean isBitSet(int value, int bit) {
        if (bit < 0 || bit > 31) {
            throw new IllegalArgumentException("bit must be 0 to 31, but was " + bit);
        }
        return ((value >>> bit) & 1) == 1;
    }

    /** How many bits are 1, in the int's 32-bit two's complement form. Works for negatives too. */
    public static int countSetBits(int value) {
        int count = 0;
        while (value != 0) {
            value &= value - 1;
            count++;
        }
        return count;
    }

    /** True for 1, 2, 4, 8, ... Never true for 0 or a negative number. */
    public static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    /** Every dock id appears twice except one. Returns that one. */
    public static int unpairedDock(int[] dockIds) {
        int result = 0;
        for (int id : dockIds) {
            result ^= id;
        }
        return result;
    }

    /** A 64-dock station: bit d of the result is 1 when dock d is occupied. Docks are 0 to 63. */
    public static long occupiedMask(int... docks) {
        long mask = 0L;
        for (int dock : docks) {
            if (dock < 0 || dock > 63) {
                throw new IllegalArgumentException("dock must be 0 to 63, but was " + dock);
            }
            mask |= 1L << dock;
        }
        return mask;
    }

    public static void main(String[] args) {
        System.out.println("Bits set in 2563: " + countSetBits(2563));
        System.out.println("Is 64 a power of two? " + isPowerOfTwo(64));
        System.out.println("Unpaired dock: " + unpairedDock(new int[] {12, 7, 40, 7, 12}));
        System.out.println("Docks 0, 3 and 40: " + Long.toBinaryString(occupiedMask(0, 3, 40)));
    }
}
