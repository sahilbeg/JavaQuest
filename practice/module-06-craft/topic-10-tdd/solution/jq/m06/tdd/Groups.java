package jq.m06.tdd;

public class Groups implements GroupDiscount {

    @Override
    public long discountedCents(long fareCents, int groupSize) {
        if (groupSize < 1) {
            throw new IllegalArgumentException("group size must be at least 1, was " + groupSize);
        }
        if (groupSize >= 5) {
            return fareCents * 70 / 100;
        }
        if (groupSize >= 3) {
            return fareCents * 80 / 100;
        }
        return fareCents;
    }
}
