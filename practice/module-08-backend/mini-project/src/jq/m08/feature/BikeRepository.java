package jq.m08.feature;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BikeRepository extends JpaRepository<Bike, String> {

    // TODO 2: add a derived count query: how many bikes at one station (by station id) have one status.
    // TODO 3: add a derived count query: the same, but the bike must also have one type.
    // TODO 4: add a derived count query: how many bikes are docked at one station, whatever their status.
}
