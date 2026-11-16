package jq.m08.apidesign;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns exceptions into RFC 9457 problem details. Each problem type has one stable URI forever. */
@RestControllerAdvice
public class ApiErrors {

    static final String PROBLEMS = "https://api.spoke.example/problems/";

    @ExceptionHandler(RentalNotFoundException.class)
    public ProblemDetail rentalNotFound(RentalNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No rental with id " + ex.rentalId() + " exists.");
        problem.setType(URI.create(PROBLEMS + "rental-not-found"));
        problem.setTitle("Rental not found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("rentalId", ex.rentalId());
        return problem;
    }

    @ExceptionHandler(InvalidRentalIdException.class)
    public ProblemDetail invalidRentalId(InvalidRentalIdException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                ex.rentalId() + " is not a rental id. Rental ids look like rental-17.");
        problem.setType(URI.create(PROBLEMS + "invalid-rental-id"));
        problem.setTitle("Invalid rental id");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
