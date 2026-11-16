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
        // TODO 3: make a 404 ProblemDetail with forStatusAndDetail, a detail that names ex.rentalId()
        // TODO 4: set type PROBLEMS + "rental-not-found", title "Rental not found", instance from request.getRequestURI()
        // TODO 5: add the extension property "rentalId" with setProperty
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidRentalIdException.class)
    public ProblemDetail invalidRentalId(InvalidRentalIdException ex, HttpServletRequest request) {
        // TODO 6: make a 400 ProblemDetail: type PROBLEMS + "invalid-rental-id", title "Invalid rental id", a detail and the instance
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
