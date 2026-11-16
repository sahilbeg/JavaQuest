package jq.m08.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data writes the code for this interface when the app starts, including your query methods. */
public interface BikeRepository extends JpaRepository<Bike, String> {

    List<Bike> findByStationIdAndStatusOrderByIdAsc(String stationId, BikeStatus status);

    long countByStatus(BikeStatus status);

    @Query("""
            select b.station.id from Bike b
            where b.type = :type and b.status = :status
            group by b.station.id
            having count(b) >= :min
            order by b.station.id""")
    List<String> findStationIdsWithAtLeast(@Param("type") BikeType type, @Param("status") BikeStatus status,
            @Param("min") long min);
}
