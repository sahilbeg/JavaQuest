package jq.m09.layered;

/** Domain layer: a rental is ACTIVE while the bike is out, and ENDED once it is docked. */
public enum RentalStatus {
    ACTIVE,
    ENDED
}
