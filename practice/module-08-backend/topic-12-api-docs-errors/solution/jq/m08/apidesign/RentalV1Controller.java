package jq.m08.apidesign;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Version 1: the fare is a flat number of cents. Deprecated, switched off after the sunset date. */
@RestController
@RequestMapping("/api/v1/rentals")
public class RentalV1Controller {

    /** RFC 9745: deprecated since 1 November 2026, 00:00 UTC, written as seconds since 1970. */
    static final String DEPRECATION = "@1793491200";
    /** RFC 8594: the date v1 stops answering, as an HTTP date. */
    static final String SUNSET = "Wed, 30 Jun 2027 23:59:59 GMT";

    public record RentalV1(String id, String bikeId, long fareCents) { }

    private final RentalService rentals;

    public RentalV1Controller(RentalService rentals) {
        this.rentals = rentals;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalV1> rental(@PathVariable String id) {
        Rental rental = rentals.find(id);
        return ResponseEntity.ok()
                .header("Deprecation", DEPRECATION)
                .header("Sunset", SUNSET)
                .body(new RentalV1(rental.id(), rental.bikeId(), rental.fareCents()));
    }
}
