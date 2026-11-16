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
        List<StationAvailability> result = new ArrayList<>();
        for (Station station : stations.findAllByOrderByNameAsc()) {
            long matching = type == null
                    ? bikes.countByStationIdAndStatus(station.getId(), BikeStatus.AVAILABLE)
                    : bikes.countByStationIdAndStatusAndType(station.getId(), BikeStatus.AVAILABLE, type);
            if (matching >= minBikes) {
                result.add(availabilityOf(station));
            }
        }
        return result;
    }

    /** One station, or a StationNotFoundException when the id is unknown. */
    public StationAvailability one(String id) {
        Station station = stations.findById(id).orElseThrow(() -> new StationNotFoundException(id));
        return availabilityOf(station);
    }

    private StationAvailability availabilityOf(Station station) {
        String id = station.getId();
        long bikesAvailable = bikes.countByStationIdAndStatus(id, BikeStatus.AVAILABLE);
        long ebikesAvailable = bikes.countByStationIdAndStatusAndType(id, BikeStatus.AVAILABLE, BikeType.EBIKE);
        long docked = bikes.countByStationId(id);
        return new StationAvailability(id, station.getName(), station.getDocks(),
                bikesAvailable, ebikesAvailable, station.getDocks() - docked);
    }
}
