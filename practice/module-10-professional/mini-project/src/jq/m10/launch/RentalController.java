package jq.m10.launch;

import java.net.URI;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Web adapter (driving side): turns HTTP into calls on the core's RentalUseCases port, and back. Nothing else. */
@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalUseCases rentals;
    private final RiderRateLimiter limiter;

    public RentalController(RentalUseCases rentals, RiderRateLimiter limiter) {
        this.rentals = rentals;
        this.limiter = limiter;
    }

    // TODO 7 (stage 3): put @Valid before each @RequestBody, so Bean Validation checks the body first.
    // TODO 8 (stage 4): add a parameter for the optional Idempotency-Key header:
    //         @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    //         and pass it to the core instead of null.
    @PostMapping
    public ResponseEntity<RentalResponse> start(@RequestBody StartRentalRequest request) {
        // TODO 15 (stage 6): first of all, call limiter.check(request.riderId()).
        // TODO 1 (stage 1): start the rental through the core: rentals.startRental(riderId, bikeId, stationId, null).
        //         Answer 201 Created with the Location /rentals/{id} and RentalResponse.from(rental) as the body.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> find(@PathVariable String id) {
        // TODO 2 (stage 1): answer 200 with RentalResponse.from(rentals.findRental(id)).
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<RentalResponse> end(@PathVariable String id, @RequestBody EndRentalRequest request) {
        // TODO 3 (stage 1): end the rental at request.stationId() through the core, and answer 200 with the ended rental.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
