package jq.m08.feature;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, String> {

    // TODO 1: add a derived query method that returns every station as a List, sorted by name from A to Z
    //         (for example List<Station> findAllByOrderByNameAsc(): By with no condition, then OrderBy).
}
