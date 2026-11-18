package jq.m10.capstone;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * The composition root for a demo: builds Bramford's core with in-memory adapters and a fixed clock,
 * then runs one ride that crosses midnight. Press Run.
 */
public class CapstoneDemo {

    public static void main(String[] args) {
        Instant start = Instant.parse("2026-11-17T23:50:00Z");
        Clock atStart = Clock.fixed(start, ZoneOffset.UTC);
        Clock atEnd = Clock.fixed(start.plusSeconds(20 * 60 + 40), ZoneOffset.UTC);

        InMemoryRentalRepository rentals = new InMemoryRentalRepository();
        InMemoryBikeInventory bikes = new InMemoryBikeInventory()
                .add(Bike.docked("BR-2003", "bramford", BikeType.EBIKE, "castle-hill"));
        RecordingEventPublisher events = new RecordingEventPublisher();
        CityConfig bramford = CityConfig.bramford();

        Rental started = new RentalService(bramford, rentals, bikes, events, atStart)
                .startRental("rider-7", "BR-2003", "castle-hill", "tap-1");
        System.out.println("started: " + started.id() + " " + started.status() + " at " + started.startedAt());

        Rental ended = new RentalService(bramford, rentals, bikes, events, atEnd)
                .endRental(started.id(), "canal-street");
        System.out.println("ended:   " + ended.id() + " " + ended.status() + ", fare " + ended.fareCents() + " cents");
        System.out.println("event:   " + events.published().getFirst());
    }
}
