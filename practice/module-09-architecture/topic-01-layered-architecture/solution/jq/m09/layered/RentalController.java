package jq.m09.layered;

import java.net.URI;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Web layer: turns HTTP into service calls, and the results back into HTTP. Nothing else. */
@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentals;

    public RentalController(RentalService rentals) {
        this.rentals = rentals;
    }

    @PostMapping
    public ResponseEntity<RentalResponse> start(@Valid @RequestBody StartRentalRequest request) {
        RentalResponse rental = rentals.start(request.riderId(), request.bikeId(), request.stationId(),
                request.paymentToken());
        return ResponseEntity.created(URI.create("/rentals/" + rental.id())).body(rental);
    }

    @GetMapping("/{id}")
    public RentalResponse find(@PathVariable String id) {
        return rentals.find(id);
    }

    @PostMapping("/{id}/end")
    public RentalResponse end(@PathVariable String id) {
        return rentals.end(id);
    }
}
