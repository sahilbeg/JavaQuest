package jq.m08.boot;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Answers the rider app's first two requests: the station list and a health check. */
// TODO 1: put @RestController on the line above the class, so Spring creates it and answers in JSON
public class StationController {

    static final List<String> STATIONS = List.of("Riverside", "Old Mill", "Harbor", "Uptown", "Depot");

    // TODO 2: put @GetMapping("/stations") on the line above this method
    public List<String> stations() {
        // TODO 3: return the STATIONS list
        return List.of();
    }

    // TODO 4: put @GetMapping("/health") on the line above this method
    public Health health() {
        // TODO 5: return new Health("UP", ...) with the number of stations in STATIONS
        return new Health("", 0);
    }
}
