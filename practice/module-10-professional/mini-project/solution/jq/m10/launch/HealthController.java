package jq.m10.launch;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health checks for Kubernetes probes. Actuator is not on this project's classpath, so they are hand-made.
 * Liveness: "is the process alive?" Readiness: "may the load balancer send me riders yet?"
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /** The JSON of a health answer, for example {"status":"UP","stationsLoaded":5}. */
    public record Health(String status, int stationsLoaded) {
    }

    private final StationDirectory stations;

    public HealthController(StationDirectory stations) {
        this.stations = stations;
    }

    @GetMapping("/live")
    public ResponseEntity<Health> live() {
        return ResponseEntity.ok(new Health("UP", stations.loadedCount()));
    }

    @GetMapping("/ready")
    public ResponseEntity<Health> ready() {
        int loaded = stations.loadedCount();
        boolean ready = loaded > 0 && loaded == stations.expectedCount();
        if (ready) {
            return ResponseEntity.ok(new Health("UP", loaded));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new Health("DOWN", loaded));
    }
}
