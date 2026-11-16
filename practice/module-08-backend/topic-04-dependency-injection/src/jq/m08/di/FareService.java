package jq.m08.di;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Works out what a ride costs. Today it builds its own helpers with new, so nobody can swap them. */
// TODO 4: put @Service above the class, so Spring creates one FareService and hands it its helpers
public class FareService {

    // TODO 5: replace these two lines with fields standard, sunday and clock, set by a constructor FareService(PriceList standard, @Qualifier("sunday") PriceList sunday, Clock clock)
    private final PriceList prices = new StandardPriceList();
    private final Clock clock = Clock.systemUTC();

    public long fareCents(BikeType type, int minutes) {
        // TODO 6: use the sunday price list when LocalDate.now(clock).getDayOfWeek() is DayOfWeek.SUNDAY, else the standard one
        return prices.unlockCents() + prices.perMinuteCents(type) * minutes;
    }
}
