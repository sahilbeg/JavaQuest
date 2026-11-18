package jq.m09.layered;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The "god controller": it does HTTP, the rules and the storage, and it sends the whole entity as JSON.
 * Split it: keep only the HTTP work here, and move the rest down into the service and the repository.
 */
@RestController
@RequestMapping("/rentals")
public class RentalController {

    // TODO 1: Delete these two storage fields. Add one field, RentalService, set by the only constructor.
    private final Map<String, Rental> rentals = new LinkedHashMap<>();
    private int lastNumber = 0;

    // TODO 2: Add @Valid before @RequestBody, so a request with a blank field gets 400 at the edge.
    // TODO 3: Call the service's start(...) and answer 201 Created, Location /rentals/{id}, with the RentalResponse.
    //         The busy-bike check moves into the service, which throws BikeUnavailableException.
    @PostMapping
    public ResponseEntity<?> start(@RequestBody StartRentalRequest request) {
        for (Rental rental : rentals.values()) {
            if (rental.isActive() && Objects.equals(rental.getBikeId(), request.bikeId())) {
                return ResponseEntity.status(409).body("Bike " + request.bikeId() + " is busy");
            }
        }
        lastNumber++;
        Rental rental = new Rental("rental-" + lastNumber, request.riderId(), request.bikeId(),
                request.stationId(), request.paymentToken());
        rentals.put(rental.getId(), rental);
        return ResponseEntity.created(URI.create("/rentals/" + rental.getId())).body(rental);
    }

    // TODO 4: Return the service's find(id). A missing rental becomes a RentalNotFoundException in the service.
    @GetMapping("/{id}")
    public ResponseEntity<?> find(@PathVariable String id) {
        Rental rental = rentals.get(id);
        if (rental == null) {
            return ResponseEntity.status(404).body("No such rental");
        }
        return ResponseEntity.ok(rental);
    }

    // TODO 5: Return the service's end(id). The "already ended" rule lives in Rental.end().
    @PostMapping("/{id}/end")
    public ResponseEntity<?> end(@PathVariable String id) {
        Rental rental = rentals.get(id);
        if (rental == null) {
            return ResponseEntity.status(404).body("No such rental");
        }
        if (!rental.isActive()) {
            return ResponseEntity.status(409).body("Already ended");
        }
        rental.end();
        return ResponseEntity.ok(rental);
    }
}
