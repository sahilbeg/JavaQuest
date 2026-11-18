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
            // TODO 5 (stage 3): add one case for each other RentalException, with the status, type and title from
            //         the README's error catalog. When all eight are there, delete the default line: the compiler
            //         then checks that you covered every kind, because RentalException is sealed.
            default -> problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Not mapped yet", ex);
        };
    }

    /** Bean Validation failed on a request body: 400 invalid-request, with an errors map from field to message, as in module 8. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        // TODO 6 (stage 3): build a 400 ProblemDetail with type PROBLEMS + "invalid-request", title "Invalid request",
        //         and a property "errors": a map from each bad field (error.getField()) to its message
        //         (error.getDefaultMessage()), sorted by field name, keeping the first message per field.
        //         Return it with handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request).
        Map<String, String> errors = new TreeMap<>();
        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }

    /** Too many POST /rentals from one rider: 429, and a Retry-After header in whole seconds. */
    @ExceptionHandler(RateLimitedException.class)
    public ResponseEntity<ProblemDetail> rateLimited(RateLimitedException ex) {
        // TODO 16 (stage 6): answer 429 with a ProblemDetail (type PROBLEMS + "rate-limited", title "Too many requests")
        //          and the header Retry-After: ex.retryAfterSeconds(). HttpHeaders.RETRY_AFTER is the header's name.
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Not mapped yet");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
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
