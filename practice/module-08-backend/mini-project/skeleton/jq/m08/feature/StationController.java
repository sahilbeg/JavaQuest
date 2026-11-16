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

    // TODO 8: map GET /api/v1/stations. minBikes is an optional query parameter, default 0, from 0 to 12 (@Min, @Max).
    //         type is an optional BikeType. Return service.all(minBikes, type).
    public List<StationAvailability> list(int minBikes, BikeType type) {
        return List.of();
    }

    // TODO 9: map GET /api/v1/stations/{id} and return service.one(id).
    public StationAvailability one(String id) {
        return null;
    }
}
