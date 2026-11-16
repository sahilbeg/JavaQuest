package jq.m08.mvc;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The rentals endpoints. Every method stays short: read the request, ask the service, answer with a status. */
@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService service;

    public RentalController(RentalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Rental> start(@RequestBody StartRental request) {
        Rental rental = service.start(request);
        URI location = URI.create("/rentals/" + rental.id());
        return ResponseEntity.created(location).body(rental);
    }

    @GetMapping("/{id}")
    public Rental find(@PathVariable String id) {
        return service.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No rental with id " + id));
    }

    @GetMapping
    public List<Rental> list(@RequestParam(required = false) String riderId) {
        if (riderId == null) {
            return service.all();
        }
        return service.forRider(riderId);
    }

    @PostMapping("/{id}/end")
    public Rental end(@PathVariable String id) {
        try {
            return service.end(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No rental with id " + id));
        } catch (RentalAlreadyEndedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
