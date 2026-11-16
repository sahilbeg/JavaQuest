package jq.m08.validation;

import java.util.Map;

import org.springframework.stereotype.Component;

/** The bikes Spoke owns. This class is finished. */
@Component
public class BikeCatalog {

    /** B-0000 sits in a test dock with a broken network cable. Asking about it crashes, the way a real bug would. */
    static final String BROKEN_DOCK_BIKE = "B-0000";

    private final Map<String, Bike> bikes = Map.of(
            "B-1042", new Bike("B-1042", "harbor", BikeStatus.AVAILABLE),
            "B-1043", new Bike("B-1043", "harbor", BikeStatus.AVAILABLE),
            "B-2001", new Bike("B-2001", "riverside", BikeStatus.RENTED),
            "B-3003", new Bike("B-3003", "depot", BikeStatus.MAINTENANCE));

    /** Returns the bike when it can be rented. Throws BikeNotFoundException or BikeUnavailableException otherwise. */
    public Bike requireAvailable(String bikeId) {
        if (BROKEN_DOCK_BIKE.equals(bikeId)) {
            throw new IllegalStateException("DockLink timeout in jq.m08.validation.BikeCatalog for " + bikeId);
        }
        Bike bike = bikes.get(bikeId);
        if (bike == null) {
            throw new BikeNotFoundException(bikeId);
        }
        if (bike.status() != BikeStatus.AVAILABLE) {
            throw new BikeUnavailableException(bikeId, bike.status());
        }
        return bike;
    }
}
