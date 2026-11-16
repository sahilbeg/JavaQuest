package jq.m08.boot;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Answers the rider app's first two requests: the station list and a health check. */
@RestController
public class StationController {

    static final List<String> STATIONS = List.of("Riverside", "Old Mill", "Harbor", "Uptown", "Depot");

    @GetMapping("/stations")
    public List<String> stations() {
        return STATIONS;
    }

    @GetMapping("/health")
    public Health health() {
        return new Health("UP", STATIONS.size());
    }
}
