package jq.m08.feature;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService service;

    public StationController(StationService service) {
        this.service = service;
    }

    @GetMapping
    public List<StationAvailability> list(
            @RequestParam(defaultValue = "0") @Min(0) @Max(12) int minBikes,
            @RequestParam(required = false) BikeType type) {
        return service.all(minBikes, type);
    }

    @GetMapping("/{id}")
    public StationAvailability one(@PathVariable String id) {
        return service.one(id);
    }
}
