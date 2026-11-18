package jq.m09.layered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// A plain unit test: no Spring, no HTTP. The service runs on the in-memory repository.
class RentalServiceTest {

    private final RentalService service = new RentalService(new InMemoryRentalRepository());

    @Test
    @DisplayName("The service starts and finds a rental without Spring or HTTP")
    void startsAndFinds() {
        RentalResponse started = service.start("rider-7", "B-1042", "harbor", "tok_visa_4242");
        assertNotNull(started, "start(...) should return a RentalResponse, not null");
        assertEquals("rental-1", started.id(), "The first rental in a new repository should be rental-1");
        assertEquals("ACTIVE", started.status(), "A new rental should be ACTIVE");

        RentalResponse found = service.find("rental-1");
        assertNotNull(found, "find(\"rental-1\") should return the rental, not null");
        assertEquals("B-1042", found.bikeId(), "find should return the rental that start saved in the repository");
    }

    @Test
    @DisplayName("The service throws BikeUnavailableException, a domain exception, for a bike that is out")
    void busyBikeThrowsDomainException() {
        service.start("rider-7", "B-1042", "harbor", "tok_visa_4242");

        BikeUnavailableException e = assertThrows(BikeUnavailableException.class,
                () -> service.start("rider-8", "B-1042", "uptown", "tok_visa_1881"),
                "Starting a second rental of B-1042 should throw BikeUnavailableException");
        assertEquals("B-1042", e.bikeId(), "The exception should name the bike");
    }

    @Test
    @DisplayName("The service throws RentalNotFoundException for an unknown id, on find and on end")
    void unknownRentalThrowsDomainException() {
        assertThrows(RentalNotFoundException.class, () -> service.find("rental-404"),
                "find of an unknown id should throw RentalNotFoundException");
        assertThrows(RentalNotFoundException.class, () -> service.end("rental-404"),
                "end of an unknown id should throw RentalNotFoundException");
    }
}
