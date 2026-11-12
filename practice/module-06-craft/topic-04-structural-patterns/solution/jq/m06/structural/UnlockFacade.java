package jq.m06.structural;

public class UnlockFacade {

    private final RiderRegistry registry;
    private final Lock lock;

    public UnlockFacade(RiderRegistry registry, Lock lock) {
        this.registry = registry;
        this.lock = lock;
    }

    public String unlock(String riderId, String bikeId) {
        if (!registry.isAllowed(riderId)) {
            return "rider " + riderId + " not allowed";
        }
        if (!lock.open(bikeId)) {
            return "lock failed for " + bikeId;
        }
        return "unlocked " + bikeId + " for " + riderId;
    }
}
