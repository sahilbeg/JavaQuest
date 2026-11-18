package jq.m10.capstone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// The two cities as data, and each city's fare rule.
class CityFaresTest {

    @Test
    @DisplayName("Bramford's config: five stations of 10 docks, London time, 120 to unlock, CLASSIC 15, EBIKE 35, no CARGO")
    void bramfordConfig() {
        CityConfig bramford = CityConfig.bramford();

        assertEquals("bramford", bramford.id(), "Bramford's id should be \"bramford\"");
        assertEquals(List.of("castle-hill", "canal-street", "market-square", "university", "bramford-central"),
                List.copyOf(bramford.stations().keySet()), "Bramford's station ids, in this order");
        assertEquals("Market Square", bramford.stations().get("market-square"),
                "market-square should be named \"Market Square\"");
        assertEquals(ZoneId.of("Europe/London"), bramford.zone(), "Bramford runs on Europe/London time");
        assertEquals(10, bramford.docksPerStation(), "Every Bramford station has 10 docks");
        assertEquals(120, bramford.unlockCents(), "Unlocking a bike in Bramford costs 120 cents");

        assertTrue(bramford.offers(BikeType.CLASSIC), "Bramford offers CLASSIC bikes");
        assertTrue(bramford.offers(BikeType.EBIKE), "Bramford offers EBIKE bikes");
        assertFalse(bramford.offers(BikeType.CARGO), "Bramford has no CARGO bikes: leave CARGO out of perMinuteCents");
        assertEquals(15L, bramford.perMinuteCents().get(BikeType.CLASSIC), "A CLASSIC minute in Bramford is 15 cents");
        assertEquals(35L, bramford.perMinuteCents().get(BikeType.EBIKE), "An EBIKE minute in Bramford is 35 cents");
    }

    @Test
    @DisplayName("Riverton's fares are unchanged: 100 to unlock, then CLASSIC 15, EBIKE 30, CARGO 25 a minute")
    void rivertonFares() {
        FareCalculator riverton = new FareCalculator(CityConfig.riverton());

        assertEquals(250, riverton.fareCents(BikeType.CLASSIC, Duration.ofMinutes(10)),
                "Riverton CLASSIC, 10 min: 100 + 15 x 10 = 250 cents");
        assertEquals(460, riverton.fareCents(BikeType.EBIKE, Duration.ofMinutes(12)),
                "Riverton EBIKE, 12 min (B-1042's usual ride): 100 + 30 x 12 = 460 cents");
        assertEquals(350, riverton.fareCents(BikeType.CARGO, Duration.ofMinutes(10)),
                "Riverton CARGO, 10 min: 100 + 25 x 10 = 350 cents");
    }

    @Test
    @DisplayName("Bramford's fares use Bramford's prices, and minutes round DOWN")
    void bramfordFaresRoundDown() {
        FareCalculator bramford = new FareCalculator(CityConfig.bramford());

        assertEquals(270, bramford.fareCents(BikeType.CLASSIC, Duration.ofMinutes(10)),
                "Bramford CLASSIC, 10 min: 120 + 15 x 10 = 270 cents");
        assertEquals(540, bramford.fareCents(BikeType.EBIKE, Duration.ofMinutes(12).plusSeconds(59)),
                "Bramford EBIKE, 12 min 59 s: whole minutes round down to 12, so 120 + 35 x 12 = 540 cents");
        assertEquals(120, bramford.fareCents(BikeType.EBIKE, Duration.ofSeconds(59)),
                "A 59-second ride is 0 whole minutes: just the 120-cent unlock");
    }

    @Test
    @DisplayName("Pricing a CARGO ride in Bramford throws BikeTypeNotOffered instead of guessing a price")
    void noCargoFareInBramford() {
        FareCalculator bramford = new FareCalculator(CityConfig.bramford());

        assertThrows(RentalException.BikeTypeNotOffered.class,
                () -> bramford.fareCents(BikeType.CARGO, Duration.ofMinutes(10)),
                "Bramford has no CARGO price. Throw RentalException.BikeTypeNotOffered, never a made-up fare or 0");
    }
}
