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
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create(PROBLEMS + "rental-not-found"));
        problem.setTitle("Rental not found");
        problem.setProperty("rentalId", ex.rentalId());
        return problem;
    }

    @ExceptionHandler(BikeUnavailableException.class)
    public ProblemDetail bikeUnavailable(BikeUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(PROBLEMS + "bike-unavailable"));
        problem.setTitle("Bike unavailable");
        problem.setProperty("bikeId", ex.bikeId());
        return problem;
    }

    @ExceptionHandler(RentalAlreadyEndedException.class)
    public ProblemDetail rentalAlreadyEnded(RentalAlreadyEndedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(PROBLEMS + "rental-already-ended"));
        problem.setTitle("Rental already ended");
        problem.setProperty("rentalId", ex.rentalId());
        return problem;
    }
}
