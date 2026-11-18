package jq.m10.capstone;

import java.time.Instant;

/**
 * The test world: one shared fleet with bikes in both cities, in-memory adapters, and a clock at 09:00 UTC.
 *
 * Bramford: BR-2003 EBIKE and BR-2002 CLASSIC at castle-hill, BR-2011 CLASSIC at canal-street,
 * BR-2900 CARGO at market-square (dropped there by mistake: Bramford has no CARGO).
 * Riverton: B-1042 EBIKE and B-2001 CLASSIC at harbor.
 */
final class World {

    final InMemoryRentalRepository rentals = new InMemoryRentalRepository();
    final InMemoryBikeInventory bikes = new InMemoryBikeInventory();
    final RecordingEventPublisher events = new RecordingEventPublisher();
    final MutableClock clock = new MutableClock(Instant.parse("2026-11-18T09:00:00Z"));

    World() {
        bikes.add(Bike.docked("BR-2003", "bramford", BikeType.EBIKE, "castle-hill"))
                .add(Bike.docked("BR-2002", "bramford", BikeType.CLASSIC, "castle-hill"))
                .add(Bike.docked("BR-2011", "bramford", BikeType.CLASSIC, "canal-street"))
                .add(Bike.docked("BR-2900", "bramford", BikeType.CARGO, "market-square"))
                .add(Bike.docked("B-1042", "riverton", BikeType.EBIKE, "harbor"))
                .add(Bike.docked("B-2001", "riverton", BikeType.CLASSIC, "harbor"));
    }

    RentalService bramford() {
        return new RentalService(CityConfig.bramford(), rentals, bikes, events, clock);
    }

    RentalService riverton() {
        return new RentalService(CityConfig.riverton(), rentals, bikes, events, clock);
    }

    BikeStatus statusOf(String bikeId) {
        return bikes.findById(bikeId).map(Bike::status).orElse(null);
    }

    String stationOf(String bikeId) {
        return bikes.findById(bikeId).map(Bike::stationId).orElse(null);
    }
}
