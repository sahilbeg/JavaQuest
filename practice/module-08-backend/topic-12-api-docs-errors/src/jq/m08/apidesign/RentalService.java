package jq.m08.apidesign;

import java.util.Map;

import org.springframework.stereotype.Service;

/** One service shared by every API version. Versions change the JSON, never the rules. */
@Service
public class RentalService {

    private final Map<String, Rental> rentals = Map.of(
            "rental-17", new Rental("rental-17", "B-1042", 460),
            "rental-18", new Rental("rental-18", "B-2001", 415));

    public Rental find(String id) {
        if (!id.matches("rental-\\d+")) {
            throw new InvalidRentalIdException(id);
        }
        Rental rental = rentals.get(id);
        if (rental == null) {
            throw new RentalNotFoundException(id);
        }
        return rental;
    }
}
