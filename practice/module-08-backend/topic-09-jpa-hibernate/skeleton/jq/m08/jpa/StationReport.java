package jq.m08.jpa;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Answers the questions the ops team asks about the fleet, using the repositories. */
@Service
public class StationReport {

    private final BikeRepository bikes;

    public StationReport(BikeRepository bikes) {
        this.bikes = bikes;
    }

    /** Ids of the AVAILABLE bikes docked at one station, sorted by id, for example [B-1042, B-1050]. */
    public List<String> freeBikeIds(String stationId) {
        // TODO 5: call your TODO 2 method with BikeStatus.AVAILABLE and turn the bikes into a list of ids
        return List.of();
    }

    /** How many bikes have each status. Every status is in the map, with 0 when no bike has it. */
    public Map<BikeStatus, Long> countByStatus() {
        Map<BikeStatus, Long> counts = new EnumMap<>(BikeStatus.class);
        // TODO 6: for each value of BikeStatus, put the count from your TODO 3 method into the map
        return counts;
    }

    /** Ids of the stations with at least min AVAILABLE e-bikes, sorted by id. */
    public List<String> stationsWithFreeEbikes(int min) {
        // TODO 7: call your TODO 4 query with BikeType.EBIKE, BikeStatus.AVAILABLE and min
        return List.of();
    }
}
