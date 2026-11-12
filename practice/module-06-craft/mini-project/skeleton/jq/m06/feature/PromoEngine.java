package jq.m06.feature;

import java.time.DayOfWeek;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromoEngine {

    private static final Logger LOG = LoggerFactory.getLogger(PromoEngine.class);

    private final List<Promo> promos;

    public PromoEngine(List<Promo> promos) {
        this.promos = List.copyOf(promos);
    }

    public Quote best(RideContext ride, long fareCents) {
        // TODO 4: pick the best applicable promo, cap it at the fare, log it, and build the quote
        return new Quote(fareCents, 0, fareCents, "none");
    }

    public static void main(String[] args) {
        PromoEngine engine = new PromoEngine(List.of(new SundayHalfPrice(), new EbikeTwentyOff(), new TenthRideFree()));
        System.out.println(engine.best(new RideContext("r1", "ebike", 25, DayOfWeek.SUNDAY, 3), 850));
        System.out.println(engine.best(new RideContext("r2", "classic", 10, DayOfWeek.MONDAY, 9), 250));
        System.out.println(engine.best(new RideContext("r3", "cargo", 10, DayOfWeek.MONDAY, 2), 350));
    }
}
