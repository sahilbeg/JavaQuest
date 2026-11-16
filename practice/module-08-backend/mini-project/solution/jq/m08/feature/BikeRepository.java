package jq.m08.feature;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BikeRepository extends JpaRepository<Bike, String> {

    long countByStationIdAndStatus(String stationId, BikeStatus status);

    long countByStationIdAndStatusAndType(String stationId, BikeStatus status, BikeType type);

    long countByStationId(String stationId);
}
