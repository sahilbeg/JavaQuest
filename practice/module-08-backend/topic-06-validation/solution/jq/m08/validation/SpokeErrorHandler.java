package jq.m08.validation;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every error into a ProblemDetail body (RFC 9457).
 * The base class already answers Spring's own errors, like broken JSON, with the right 4xx status.
 */
@RestControllerAdvice
public class SpokeErrorHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SpokeErrorHandler.class);
    private static final String PROBLEMS = "https://api.spoke.example/problems/";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = e.getBody();
        problem.setType(URI.create(PROBLEMS + "invalid-request"));
        problem.setTitle("Invalid request");
        problem.setDetail("Some fields are missing or wrong. See errors for each one.");
        Map<String, String> errors = new TreeMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(BikeNotFoundException.class)
    public ProblemDetail bikeNotFound(BikeNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setType(URI.create(PROBLEMS + "bike-not-found"));
        problem.setTitle("Bike not found");
        return problem;
    }

    @ExceptionHandler(BikeUnavailableException.class)
    public ProblemDetail bikeUnavailable(BikeUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setType(URI.create(PROBLEMS + "bike-unavailable"));
        problem.setTitle("Bike unavailable");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception e) {
        log.error("Unexpected error while handling a request", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side");
    }
}
