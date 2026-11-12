package jq.m06.junit;

/** Works out the price of one ride, in cents. */
public interface FareCalculator {

    /**
     * @param type       "classic", "ebike" or "cargo"
     * @param minutes    how long the ride took, 0 or more
     * @param membership "BASIC", "PLUS" or "PRO"
     * @return the fare in cents, so 250 means $2.50
     */
    long fareCents(String type, int minutes, String membership);
}
