package jq.m10.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// startRental on Bramford's core, with in-memory adapters. The fleet is in World.
class StartRentalTest {

    private final World world = new World();
    private final RentalService bramford = world.bramford();

    @Test
    @DisplayName("A Bramford rental starts: a new ACTIVE rental is saved, and the bike is RENTED")
    void startsARental() {
        Rental rental = bramford.startRental("rider-7", "BR-2003", "castle-hill", null);

        assertNotNull(rental, "startRental should return the new rental, not null");
        assertEquals("rental-1", rental.id(), "The id should come from rentals.nextId(), so the first one is rental-1");
        assertEquals("bramford", rental.cityId(), "The rental belongs to the core's city");
        assertEquals("rider-7", rental.riderId(), "riderId should be the rider who asked");
        assertEquals(BikeType.EBIKE, rental.bikeType(), "bikeType should be copied from the bike: BR-2003 is an EBIKE");
        assertEquals("castle-hill", rental.startStationId(), "startStationId should be where the ride starts");
        assertEquals(Instant.parse("2026-11-18T09:00:00Z"), rental.startedAt(), "startedAt should come from the clock");
        assertTrue(rental.isActive(), "A new rental is ACTIVE");
        assertTrue(world.rentals.findById("rental-1").isPresent(), "The rental should be saved in the repository");
        assertEquals(BikeStatus.RENTED, world.statusOf("BR-2003"), "BR-2003 should be RENTED once the ride starts");
    }

    @Test
    @DisplayName("A station from another city is rejected with StationNotFound, and nothing changes")
    void foreignStationRejected() {
        assertThrows(RentalException.StationNotFound.class,
                () -> bramford.startRental("rider-7", "BR-2003", "harbor", null),
                "harbor is a Riverton station. Bramford's core should throw RentalException.StationNotFound");
        assertEquals(0, world.rentals.size(), "A rejected request must not save a rental");
        assertEquals(BikeStatus.AVAILABLE, world.statusOf("BR-2003"), "A rejected request must leave the bike AVAILABLE");
    }

    @Test
    @DisplayName("A bike from another city counts as not found: BikeNotFound")
    void foreignBikeRejected() {
        assertThrows(RentalException.BikeNotFound.class,
                () -> bramford.startRental("rider-7", "B-1042", "castle-hill", null),
                "B-1042 is a Riverton bike. Bramford's core should throw RentalException.BikeNotFound");
        assertThrows(RentalException.BikeNotFound.class,
                () -> bramford.startRental("rider-7", "BR-9999", "castle-hill", null),
                "BR-9999 does not exist. Throw RentalException.BikeNotFound");
        assertEquals(BikeStatus.AVAILABLE, world.statusOf("B-1042"), "Riverton's B-1042 must not be touched");
    }

    @Test
    @DisplayName("A CARGO bike dropped in Bramford by mistake cannot be rented: BikeTypeNotOffered")
    void noCargoInBramford() {
        assertThrows(RentalException.BikeTypeNotOffered.class,
                () -> bramford.startRental("rider-7", "BR-2900", "market-square", null),
                "Bramford offers no CARGO bikes. Throw RentalException.BikeTypeNotOffered");
        assertEquals(0, world.rentals.size(), "No rental should be saved for a CARGO bike in Bramford");
    }

    @Test
    @DisplayName("A bike that is already out, or docked at another station, is BikeUnavailable")
    void doubleRentRejected() {
        assertNotNull(bramford.startRental("rider-7", "BR-2003", "castle-hill", null),
                "The first rental of BR-2003 should work");

        assertThrows(RentalException.BikeUnavailable.class,
                () -> bramford.startRental("rider-8", "BR-2003", "castle-hill", null),
                "BR-2003 is already RENTED. A second rider must get RentalException.BikeUnavailable");
        assertThrows(RentalException.BikeUnavailable.class,
                () -> bramford.startRental("rider-8", "BR-2011", "castle-hill", null),
                "BR-2011 is docked at canal-street, not castle-hill. Throw RentalException.BikeUnavailable");
        assertEquals(1, world.rentals.size(), "Only the first rental should be saved");
    }

    @Test
    @DisplayName("A rider who is already riding cannot start a second rental: ActiveRentalExists")
    void oneRideAtATime() {
        assertNotNull(bramford.startRental("rider-7", "BR-2003", "castle-hill", null),
                "rider-7's first rental should work");

        assertThrows(RentalException.ActiveRentalExists.class,
                () -> bramford.startRental("rider-7", "BR-2002", "castle-hill", null),
                "rider-7 is still riding BR-2003. Throw RentalException.ActiveRentalExists");
        assertEquals(BikeStatus.AVAILABLE, world.statusOf("BR-2002"), "BR-2002 must stay AVAILABLE");
    }

    @Test
    @DisplayName("A retry with the same idempotency key returns the same rental and creates nothing new")
    void idempotentRetry() {
        Rental first = bramford.startRental("rider-7", "BR-2003", "castle-hill", "tap-7c1e");
        assertNotNull(first, "The first request should start a rental");
        assertEquals("tap-7c1e", first.idempotencyKey(), "The rental should remember its idempotency key");

        world.clock.advance(Duration.ofSeconds(3));
        Rental retry = bramford.startRental("rider-7", "BR-2003", "castle-hill", "tap-7c1e");

        assertEquals(first, retry, "A retry with the same key, bike and station should return the very same rental");
        assertEquals(1, world.rentals.size(), "A retry must not save a second rental");
    }

    @Test
    @DisplayName("The same key with a different bike is IdempotencyKeyReused, but keys of different riders never mix")
    void keyReusedForDifferentRequest() {
        assertNotNull(bramford.startRental("rider-7", "BR-2003", "castle-hill", "tap-7c1e"),
                "The first request should start a rental");

        assertThrows(RentalException.IdempotencyKeyReused.class,
                () -> bramford.startRental("rider-7", "BR-2002", "castle-hill", "tap-7c1e"),
                "Same rider, same key, different bike: that is a bug in the app. Throw IdempotencyKeyReused");

        Rental other = bramford.startRental("rider-8", "BR-2002", "castle-hill", "tap-7c1e");
        assertNotNull(other, "rider-8 may use the same key text: keys belong to one rider");
        assertEquals("rider-8", other.riderId(), "rider-8 should get a rental of their own");
        assertNull(world.rentals.findById("rental-3").orElse(null), "Exactly two rentals should exist");
    }
}
