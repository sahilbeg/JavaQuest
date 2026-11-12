package jq.m06.structural;

import java.util.Set;

public class RiderRegistry {

    private final Set<String> allowedRiders;

    public RiderRegistry(Set<String> allowedRiders) {
        this.allowedRiders = Set.copyOf(allowedRiders);
    }

    public boolean isAllowed(String riderId) {
        return allowedRiders.contains(riderId);
    }
}
