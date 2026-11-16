package jq.m08.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data writes the code for this interface when the app starts, including your query methods. */
public interface BikeRepository extends JpaRepository<Bike, String> {

    // TODO 2: declare a derived query that finds bikes by station id and status, sorted by id (findByStationId...)

    // TODO 3: declare a derived query that counts the bikes with one status (countBy...)

    // TODO 4: declare a JPQL @Query that returns the ids of stations with at least :min bikes of a :type and :status
}
