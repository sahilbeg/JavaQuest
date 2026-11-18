package jq.m10.launch;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Web adapter: the live numbers for one Bramford station, for the map in the rider app. */
@RestController
public class StationController {

    private final RentalUseCases rentals;

    public StationController(RentalUseCases rentals) {
        this.rentals = rentals;
    }

    @GetMapping("/stations/{id}/availability")
    public ResponseEntity<StationAvailability> availability(@PathVariable String id) {
        // TODO 4 (stage 2): answer 200 with the core's numbers for this station: rentals.availability(id).
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
