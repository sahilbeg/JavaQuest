package jq.m08.feature;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StationService {

    private final StationRepository stations;
    private final BikeRepository bikes;

    public StationService(StationRepository stations, BikeRepository bikes) {
        this.stations = stations;
        this.bikes = bikes;
    }

    /** Every station, sorted by name, with at least minBikes available bikes (of that type, when type is not null). */
    public List<StationAvailability> all(int minBikes, BikeType type) {
        // TODO 6: go through the stations sorted by name. Keep a station when its count of AVAILABLE bikes
        //         (of that type, when type is not null) is at least minBikes. Return availabilityOf each one you keep.
        return List.of();
    }

    /** One station, or a StationNotFoundException when the id is unknown. */
    public StationAvailability one(String id) {
        // TODO 7: find the station by id and return its availability, or throw new StationNotFoundException(id).
        return null;
    }

    private StationAvailability availabilityOf(Station station) {
        // TODO 5: build the record with your three count queries. docksFree is the station's docks minus its docked bikes.
        return null;
    }
}
