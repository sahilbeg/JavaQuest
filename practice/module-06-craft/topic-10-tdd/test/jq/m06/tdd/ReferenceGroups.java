package jq.m06.tdd;

class ReferenceGroups implements GroupDiscount {

    @Override
    public long discountedCents(long fareCents, int groupSize) {
        if (groupSize < 1) {
            throw new IllegalArgumentException("group size must be at least 1");
        }
        long percentPaid = 100;
        if (groupSize >= 5) {
            percentPaid = 70;
        } else if (groupSize >= 3) {
            percentPaid = 80;
        }
        return Math.floorDiv(fareCents * percentPaid, 100);
    }
}
