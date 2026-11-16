package jq.m08.di;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Works out what a ride costs. Spring hands in the price lists and the clock. */
@Service
public class FareService {

    private final PriceList standard;
    private final PriceList sunday;
    private final Clock clock;

    public FareService(PriceList standard, @Qualifier("sunday") PriceList sunday, Clock clock) {
        this.standard = standard;
        this.sunday = sunday;
        this.clock = clock;
    }

    public long fareCents(BikeType type, int minutes) {
        boolean isSunday = LocalDate.now(clock).getDayOfWeek() == DayOfWeek.SUNDAY;
        PriceList prices = isSunday ? sunday : standard;
        return prices.unlockCents() + prices.perMinuteCents(type) * minutes;
    }
}
