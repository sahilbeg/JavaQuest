package jq.m10.launch;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
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
 * Web adapter: the one place where errors become HTTP status codes and Problem Details.
 * It extends ResponseEntityExceptionHandler, so Spring's own errors (bad JSON, wrong method) are Problem Details too.
 */
@RestControllerAdvice
public class LaunchProblems extends ResponseEntityExceptionHandler {

    static final String PROBLEMS = "https://api.spoke.example/problems/";

    /** Every business-rule error from the core. RentalException is sealed, so the switch must cover them all. */
    @ExceptionHandler(RentalException.class)
    public ProblemDetail rentalProblem(RentalException ex) {
        return switch (ex) {
            case RentalException.RentalNotFound e -> problem(HttpStatus.NOT_FOUND, "rental-not-found", "Rental not found", e);
            case RentalException.StationNotFound e -> problem(HttpStatus.NOT_FOUND, "station-not-found", "Station not found", e);
            case RentalException.BikeNotFound e -> problem(HttpStatus.NOT_FOUND, "bike-not-found", "Bike not found", e);
            case RentalException.BikeTypeNotOffered e ->
                    problem(HttpStatus.UNPROCESSABLE_CONTENT, "bike-type-not-offered", "Bike type not offered", e);
            case RentalException.BikeUnavailable e -> problem(HttpStatus.CONFLICT, "bike-unavailable", "Bike unavailable", e);
            case RentalException.ActiveRentalExists e ->
                    problem(HttpStatus.CONFLICT, "active-rental-exists", "Rider already has an active rental", e);
            case RentalException.RentalAlreadyEnded e ->
                    problem(HttpStatus.CONFLICT, "rental-already-ended", "Rental already ended", e);
            case RentalException.IdempotencyKeyReused e ->
                    problem(HttpStatus.UNPROCESSABLE_CONTENT, "idempotency-key-reused", "Idempotency key reused", e);
        };
    }

    /** Bean Validation failed on a request body: 400 invalid-request, with an errors map from field to message, as in module 8. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Some fields are missing or blank. See errors for each one.");
        problem.setType(URI.create(PROBLEMS + "invalid-request"));
        problem.setTitle("Invalid request");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    /** Too many POST /rentals from one rider: 429, and a Retry-After header in whole seconds. */
    @ExceptionHandler(RateLimitedException.class)
    public ResponseEntity<ProblemDetail> rateLimited(RateLimitedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setType(URI.create(PROBLEMS + "rate-limited"));
        problem.setTitle("Too many requests");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.retryAfterSeconds()))
                .body(problem);
    }

    /**
     * The database refused a write that broke a unique constraint, for example a second active rental
     * for one bike sent by another server at the same moment. That is a conflict, not a crash.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflict(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Another request changed the same rental or bike first. Try again.");
        problem.setType(URI.create(PROBLEMS + "conflict"));
        problem.setTitle("Conflict");
        return problem;
    }

    /** Anything else is our bug. Answer 500 and keep the internals (stack traces, SQL) out of the answer. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side.");
        problem.setType(URI.create(PROBLEMS + "internal-error"));
        problem.setTitle("Internal error");
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, RentalException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setType(URI.create(PROBLEMS + type));
        problem.setTitle(title);
        return problem;
    }
}
