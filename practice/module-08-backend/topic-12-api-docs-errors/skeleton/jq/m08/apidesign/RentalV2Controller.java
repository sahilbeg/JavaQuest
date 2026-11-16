package jq.m08.apidesign;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Version 2: the fare is an object with an amount and a currency, ready for other cities. */
@RestController
@RequestMapping("/api/v2/rentals")
public class RentalV2Controller {

    public record Fare(long amountCents, String currency) { }

    public record RentalV2(String id, String bikeId, Fare fare) { }

    private final RentalService rentals;

    public RentalV2Controller(RentalService rentals) {
        this.rentals = rentals;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalV2> rental(@PathVariable String id) {
        Rental rental = rentals.find(id);
        // TODO 2: build a Fare from rental.fareCents() with the currency "USD"
        Fare fare = null;
        return ResponseEntity.ok(new RentalV2(rental.id(), rental.bikeId(), fare));
    }
}
