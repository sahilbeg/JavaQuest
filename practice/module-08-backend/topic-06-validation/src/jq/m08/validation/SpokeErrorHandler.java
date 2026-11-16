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

    // Spring calls this when @Valid finds problems. The problem already has status 400.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = e.getBody();
        // TODO 3: set the type to PROBLEMS + "invalid-request", the title to "Invalid request" and a short, friendly detail
        // TODO 4: build a Map of field name -> message from e.getBindingResult().getFieldErrors(), then problem.setProperty("errors", map)
        return ResponseEntity.status(status).body(problem);
    }

    // TODO 5: add an @ExceptionHandler for BikeNotFoundException that returns a 404 ProblemDetail
    //         with type PROBLEMS + "bike-not-found", title "Bike not found" and the exception's message as the detail

    // TODO 6: add an @ExceptionHandler for BikeUnavailableException that returns a 409 ProblemDetail
    //         with type PROBLEMS + "bike-unavailable", title "Bike unavailable" and the exception's message as the detail

    // TODO 7: add a catch-all @ExceptionHandler(Exception.class) that logs the exception with log.error
    //         and returns a 500 ProblemDetail whose detail is "Something went wrong on our side", and nothing else from the exception
}
