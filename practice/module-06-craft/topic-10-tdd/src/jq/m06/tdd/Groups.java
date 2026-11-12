package jq.m06.tdd;

public class Groups implements GroupDiscount {

    @Override
    public long discountedCents(long fareCents, int groupSize) {
        // TODO 6: when groupSize is below 1, throw an IllegalArgumentException
        // TODO 7: 5 or more riders pay 70 percent of the fare, 3 or 4 riders pay 80 percent
        // TODO 8: 1 or 2 riders pay the full fare; round every result down to whole cents
        return 0;
    }
}
