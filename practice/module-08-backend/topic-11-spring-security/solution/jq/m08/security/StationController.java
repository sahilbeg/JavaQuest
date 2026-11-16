package jq.m08.security;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public data: anyone may see the stations, logged in or not. */
@RestController
public class StationController {

    public record Station(String id, String name, int freeDocks) { }

    @GetMapping("/stations")
    public List<Station> stations() {
        return List.of(
                new Station("riverside", "Riverside", 7),
                new Station("old-mill", "Old Mill", 3),
                new Station("harbor", "Harbor", 12),
                new Station("uptown", "Uptown", 0),
                new Station("depot", "Depot", 9));
    }
}
