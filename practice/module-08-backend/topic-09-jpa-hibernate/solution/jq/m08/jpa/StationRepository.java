package jq.m08.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data writes the code for this interface when the app starts. */
public interface StationRepository extends JpaRepository<Station, String> {
}
