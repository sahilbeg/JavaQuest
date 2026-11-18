package jq.m10.capstone;

import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Core (domain): everything that differs between cities, as data.
 * A new city is a new CityConfig, not a new if-statement in the rental code.
 *
 * @param perMinuteCents the price of one minute for each bike type the city offers. A type that is missing is not offered.
 * @param stations       station id to station name, in a fixed order
 */
public record CityConfig(String id, String name, ZoneId zone, long unlockCents,
                         Map<BikeType, Long> perMinuteCents, Map<String, String> stations, int docksPerStation) {

    public CityConfig {
        perMinuteCents = perMinuteCents.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new EnumMap<>(perMinuteCents));
        stations = Collections.unmodifiableMap(new LinkedHashMap<>(stations));
    }

    public boolean offers(BikeType type) {
        return perMinuteCents.containsKey(type);
    }

    public boolean hasStation(String stationId) {
        return stations.containsKey(stationId);
    }

    /** Riverton, Spoke's first city: 100 cents to unlock, then CLASSIC 15, EBIKE 30, CARGO 25 cents a minute. */
    public static CityConfig riverton() {
        Map<String, String> stations = new LinkedHashMap<>();
        stations.put("riverside", "Riverside");
        stations.put("old-mill", "Old Mill");
        stations.put("harbor", "Harbor");
        stations.put("uptown", "Uptown");
        stations.put("depot", "Depot");
        return new CityConfig("riverton", "Riverton", ZoneId.of("America/New_York"), 100,
                Map.of(BikeType.CLASSIC, 15L, BikeType.EBIKE, 30L, BikeType.CARGO, 25L), stations, 12);
    }

    /** Bramford, the second city: 120 cents to unlock, then CLASSIC 15 and EBIKE 35 cents a minute. No CARGO. */
    public static CityConfig bramford() {
        // TODO 1: Build Bramford's config, the same way riverton() does above.
        //         id "bramford", name "Bramford", zone Europe/London, 120 cents to unlock, 10 docks per station.
        //         Prices: CLASSIC 15 and EBIKE 35 cents a minute. Leave CARGO out: Bramford does not offer it.
        //         Stations, in this order: castle-hill (Castle Hill), canal-street (Canal Street),
        //         market-square (Market Square), university (University), bramford-central (Bramford Central).
        Map<String, String> stations = new LinkedHashMap<>();
        return new CityConfig("bramford", "Bramford", ZoneId.of("UTC"), 0, Map.of(), stations, 0);
    }
}
