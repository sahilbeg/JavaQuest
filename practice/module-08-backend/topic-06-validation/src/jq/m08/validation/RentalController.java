package jq.m08.validation;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final BikeCatalog catalog;
    private final AtomicInteger nextNumber = new AtomicInteger(1);

    public RentalController(BikeCatalog catalog) {
        this.catalog = catalog;
    }

    // TODO 2: put @Valid (from jakarta.validation) in front of @RequestBody, so Spring checks the request before this method runs
    @PostMapping
    public ResponseEntity<Rental> start(@RequestBody StartRentalRequest request) {
        catalog.requireAvailable(request.bikeId());
        Rental rental = new Rental("rental-" + nextNumber.getAndIncrement(), request.riderId(), request.bikeId(), request.station());
        return ResponseEntity.created(URI.create("/rentals/" + rental.id())).body(rental);
    }
}
