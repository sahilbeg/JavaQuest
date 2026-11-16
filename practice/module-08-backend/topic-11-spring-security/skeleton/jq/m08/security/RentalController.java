package jq.m08.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** A rider's own rentals. The rider comes from the login, never from the URL. */
@RestController
public class RentalController {

    public record MyRentals(String rider, List<String> rentalIds) { }

    private static final Map<String, List<String>> RENTALS = Map.of(
            "ada", List.of("rental-101", "rental-117"),
            "theo", List.of());

    @GetMapping("/rentals/mine")
    public MyRentals mine(Authentication auth) {
        // auth is null only while security is switched off (no @EnableWebSecurity yet)
        String rider = auth == null ? "nobody" : auth.getName();
        return new MyRentals(rider, RENTALS.getOrDefault(rider, List.of()));
    }
}
