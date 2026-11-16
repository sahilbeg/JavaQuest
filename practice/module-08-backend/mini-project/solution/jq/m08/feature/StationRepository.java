package jq.m08.feature;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, String> {

    List<Station> findAllByOrderByNameAsc();
}
