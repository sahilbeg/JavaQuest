package jq.m10.launch;

import java.net.URI;

import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<RentalResponse> start(@Valid @RequestBody StartRentalRequest request,
                                                @RequestHeader(value = "Idempotency-Key", required = false)
                                                String idempotencyKey) {
        limiter.check(request.riderId());
        Rental rental = rentals.startRental(request.riderId(), request.bikeId(), request.stationId(), idempotencyKey);
        return ResponseEntity.created(URI.create("/rentals/" + rental.id())).body(RentalResponse.from(rental));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> find(@PathVariable String id) {
        return ResponseEntity.ok(RentalResponse.from(rentals.findRental(id)));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<RentalResponse> end(@PathVariable String id, @Valid @RequestBody EndRentalRequest request) {
        return ResponseEntity.ok(RentalResponse.from(rentals.endRental(id, request.stationId())));
    }
}
