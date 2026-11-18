package jq.m10.launch;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Which of the city's stations this server has loaded. In production they come from the station table
 * when the server starts; here loadAll() stands in for that. The readiness check reads loadedCount().
 */
public class StationDirectory {

    private final CityConfig city;
    private final Set<String> loaded = new LinkedHashSet<>();

    public StationDirectory(CityConfig city) {
        this.city = city;
    }

    /** Loads one station. Throws IllegalArgumentException for a station the city does not have. */
    public synchronized void load(String stationId) {
        if (!city.hasStation(stationId)) {
            throw new IllegalArgumentException("No station " + stationId + " in " + city.id());
        }
        loaded.add(stationId);
    }

    public void loadAll() {
        city.stations().keySet().forEach(this::load);
    }

    public synchronized int loadedCount() {
        return loaded.size();
    }

    /** How many stations the city has, so how many must be loaded before the server is ready. */
    public int expectedCount() {
        return city.stations().size();
    }
}
