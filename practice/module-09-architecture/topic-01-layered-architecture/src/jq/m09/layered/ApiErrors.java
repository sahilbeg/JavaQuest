package jq.m09.layered;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Web layer: the one place where domain exceptions become HTTP status codes and Problem Details. */
@RestControllerAdvice
public class ApiErrors {

    static final String PROBLEMS = "https://api.spoke.example/problems/";

    @ExceptionHandler(RentalNotFoundException.class)
    public ProblemDetail rentalNotFound(RentalNotFoundException ex) {
        // TODO 11: 404, type PROBLEMS + "rental-not-found", title "Rental not found", property "rentalId".
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BikeUnavailableException.class)
    public ProblemDetail bikeUnavailable(BikeUnavailableException ex) {
        // TODO 12: 409, type PROBLEMS + "bike-unavailable", title "Bike unavailable", property "bikeId".
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RentalAlreadyEndedException.class)
    public ProblemDetail rentalAlreadyEnded(RentalAlreadyEndedException ex) {
        // TODO 13: 409, type PROBLEMS + "rental-already-ended", title "Rental already ended", property "rentalId".
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
