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
        Promo bestPromo = null;
        long bestDiscount = 0;
        for (Promo promo : promos) {
            if (!promo.appliesTo(ride)) {
                continue;
            }
            long discount = Math.min(fareCents, promo.discountCents(ride, fareCents));
            boolean larger = discount > bestDiscount;
            boolean tieWins = bestPromo != null && discount == bestDiscount && promo.name().compareTo(bestPromo.name()) < 0;
            if (bestPromo == null || larger || tieWins) {
                bestPromo = promo;
                bestDiscount = discount;
            }
        }
        if (bestPromo == null) {
            return new Quote(fareCents, 0, fareCents, "none");
        }
        LOG.info("promo {} saved {} cents for {}", bestPromo.name(), bestDiscount, ride.riderId());
        return new Quote(fareCents, bestDiscount, fareCents - bestDiscount, bestPromo.name());
    }

    public static void main(String[] args) {
        PromoEngine engine = new PromoEngine(List.of(new SundayHalfPrice(), new EbikeTwentyOff(), new TenthRideFree()));
        System.out.println(engine.best(new RideContext("r1", "ebike", 25, DayOfWeek.SUNDAY, 3), 850));
        System.out.println(engine.best(new RideContext("r2", "classic", 10, DayOfWeek.MONDAY, 9), 250));
        System.out.println(engine.best(new RideContext("r3", "cargo", 10, DayOfWeek.MONDAY, 2), 350));
    }
}
