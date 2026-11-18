package jq.m10.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// endRental on the core: the fare from the injected clock, the bike docked again, and one RideEnded event.
class EndRentalTest {

    private final World world = new World();

    private Rental startAt(RentalService city, String instant, String riderId, String bikeId, String stationId) {
        world.clock.set(Instant.parse(instant));
        Rental rental = city.startRental(riderId, bikeId, stationId, null);
        assertNotNull(rental, "startRental should return the new rental before it can be ended");
        return rental;
    }

    @Test
    @DisplayName("Ending a Bramford EBIKE ride after 12 min 40 s charges 540 cents and docks the bike at the end station")
    void endChargesAndDocks() {
        RentalService bramford = world.bramford();
        Rental started = startAt(bramford, "2026-11-18T09:00:00Z", "rider-7", "BR-2003", "castle-hill");

        world.clock.advance(Duration.ofMinutes(12).plusSeconds(40));
        Rental ended = bramford.endRental(started.id(), "canal-street");

        assertNotNull(ended, "endRental should return the ended rental");
        assertFalse(ended.isActive(), "The returned rental should be ENDED");
        assertEquals(540, ended.fareCents(), "12 whole minutes on a Bramford EBIKE: 120 + 35 x 12 = 540 cents");
        assertEquals("canal-street", ended.endStationId(), "endStationId should be where the bike was docked");
        assertEquals(Instant.parse("2026-11-18T09:12:40Z"), ended.endedAt(), "endedAt should come from the clock");
        assertEquals(ended, world.rentals.findById(started.id()).orElse(null),
                "The ended rental should be saved in the repository");
        assertEquals(BikeStatus.AVAILABLE, world.statusOf("BR-2003"), "The bike should be AVAILABLE again");
        assertEquals("canal-street", world.stationOf("BR-2003"), "The bike should now be docked at canal-street");
    }

    @Test
    @DisplayName("Ending a ride publishes exactly one RideEnded event with the fare and the minutes")
    void endPublishesRideEnded() {
        RentalService bramford = world.bramford();
        Rental started = startAt(bramford, "2026-11-18T09:00:00Z", "rider-7", "BR-2003", "castle-hill");
        world.clock.advance(Duration.ofMinutes(12).plusSeconds(40));

        bramford.endRental(started.id(), "canal-street");

        List<RideEnded> events = world.events.published();
        assertEquals(1, events.size(), "Ending one ride should publish exactly one RideEnded event");
        RideEnded expected = new RideEnded("ride-ended:" + started.id(), "bramford", started.id(), "rider-7",
                "BR-2003", BikeType.EBIKE, 12, 540, LocalDate.parse("2026-11-18"));
        assertEquals(expected, events.getFirst(),
                "The event should carry: eventId \"ride-ended:\" + rental id, city, rental, rider, bike, type, "
                        + "whole minutes (12), fare (540) and the service date");
    }

    @Test
    @DisplayName("A Bramford ride from 23:50 to 00:10 is 20 minutes and belongs to the day it started")
    void rideAcrossMidnight() {
        RentalService bramford = world.bramford();
        Rental started = startAt(bramford, "2026-11-17T23:50:00Z", "rider-9", "BR-2003", "castle-hill");

        world.clock.set(Instant.parse("2026-11-18T00:10:30Z"));
        Rental ended = bramford.endRental(started.id(), "castle-hill");

        assertNotNull(ended, "endRental should return the ended rental");
        assertEquals(820, ended.fareCents(), "23:50 to 00:10:30 is 20 whole minutes: 120 + 35 x 20 = 820 cents");
        List<RideEnded> events = world.events.published();
        assertEquals(1, events.size(), "One ride, one RideEnded event");
        assertEquals(LocalDate.parse("2026-11-17"), events.getFirst().serviceDate(),
                "The ride started on 17 November in London, so its serviceDate is 2026-11-17, not the 18th");
    }

    @Test
    @DisplayName("A Riverton ride at 04:30 UTC belongs to the evening before, New York time")
    void rivertonUsesItsOwnZone() {
        RentalService riverton = world.riverton();
        Rental started = startAt(riverton, "2026-11-18T04:30:00Z", "rider-7", "B-1042", "harbor");

        world.clock.advance(Duration.ofMinutes(12).plusSeconds(5));
        Rental ended = riverton.endRental(started.id(), "uptown");

        assertNotNull(ended, "endRental should return the ended rental");
        assertEquals(460, ended.fareCents(), "Riverton EBIKE, 12 min: 100 + 30 x 12 = 460 cents");
        List<RideEnded> events = world.events.published();
        assertEquals(1, events.size(), "One ride, one RideEnded event");
        assertEquals(LocalDate.parse("2026-11-17"), events.getFirst().serviceDate(),
                "04:30 UTC is 23:30 on 17 November in New York. Use the city's zone: startedAt.atZone(city.zone())");
    }

    @Test
    @DisplayName("Ending an unknown rental, or another city's rental, throws RentalNotFound")
    void unknownRental() {
        assertThrows(RentalException.RentalNotFound.class,
                () -> world.bramford().endRental("rental-999", "castle-hill"),
                "There is no rental-999. Throw RentalException.RentalNotFound");

        Rental riverton = startAt(world.riverton(), "2026-11-18T09:00:00Z", "rider-7", "B-1042", "harbor");
        assertThrows(RentalException.RentalNotFound.class,
                () -> world.bramford().endRental(riverton.id(), "castle-hill"),
                "Bramford's core must not see a Riverton rental. Throw RentalException.RentalNotFound");
        assertEquals(0, world.events.published().size(), "No ride ended, so no event");
    }

    @Test
    @DisplayName("Ending a ride twice throws RentalAlreadyEnded and never charges or publishes twice")
    void endTwice() {
        RentalService bramford = world.bramford();
        Rental started = startAt(bramford, "2026-11-18T09:00:00Z", "rider-7", "BR-2002", "castle-hill");
        world.clock.advance(Duration.ofMinutes(10));
        assertNotNull(bramford.endRental(started.id(), "university"), "The first end should work");

        world.clock.advance(Duration.ofMinutes(5));
        assertThrows(RentalException.RentalAlreadyEnded.class,
                () -> bramford.endRental(started.id(), "university"),
                "The rental has already ended. Throw RentalException.RentalAlreadyEnded");
        assertEquals(270, world.rentals.findById(started.id()).orElseThrow().fareCents(),
                "The stored fare must stay 270 (CLASSIC, 10 min). A second end must not change it");
        assertEquals(1, world.events.published().size(), "Only the first end may publish RideEnded");
    }
}
