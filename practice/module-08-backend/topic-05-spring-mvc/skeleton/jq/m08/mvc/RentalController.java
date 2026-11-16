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

    // TODO 1: map this method to POST /rentals with @PostMapping, and put @RequestBody on the request parameter
    public ResponseEntity<Rental> start(StartRental request) {
        // TODO 2: call service.start(request), then answer 201 Created with a Location of /rentals/{id} and the rental as the body
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // TODO 3: map this method to GET /rentals/{id} and read the id with @PathVariable
    public Rental find(String id) {
        // TODO 4: return the rental from service.find(id), or throw new ResponseStatusException(HttpStatus.NOT_FOUND, ...) when it is empty
        return null;
    }

    // TODO 5: map this method to GET /rentals and read riderId with @RequestParam(required = false)
    public List<Rental> list(String riderId) {
        // TODO 6: return service.all() when riderId is null, otherwise service.forRider(riderId)
        return List.of();
    }

    // TODO 7: map this method to POST /rentals/{id}/end and read the id with @PathVariable
    public Rental end(String id) {
        // TODO 8: return the ended rental from service.end(id): 404 when it is empty, 409 Conflict when it throws RentalAlreadyEndedException
        return null;
    }
}
