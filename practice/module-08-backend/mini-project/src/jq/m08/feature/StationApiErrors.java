package jq.m08.feature;

import java.net.URI;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Turns the API's errors into RFC 9457 problem details. */
@RestControllerAdvice
public class StationApiErrors {

    static final URI STATION_NOT_FOUND = URI.create("https://api.spoke.example/problems/station-not-found");
    static final URI INVALID_REQUEST = URI.create("https://api.spoke.example/problems/invalid-request");

    // TODO 10: make this handle StationNotFoundException: 404, type STATION_NOT_FOUND, title "Station not found",
    //          detail e.getMessage(), and the extra property "stationId".
    public ProblemDetail stationNotFound(StationNotFoundException e) {
        return null;
    }

    // TODO 11: make this handle MethodArgumentTypeMismatchException (?minBikes=abc, ?type=SCOOTER).
    //          e.getName() is the query parameter's name. Return invalid(name, a sentence for a human).
    public ProblemDetail wrongType(MethodArgumentTypeMismatchException e) {
        return null;
    }

    // TODO 12: make this handle HandlerMethodValidationException (?minBikes=13). Take the first validation result,
    //          read its parameter's name, and return invalid(name, a sentence).
    public ProblemDetail outOfRange(HandlerMethodValidationException e) {
        return null;
    }

    private static ProblemDetail invalid(String parameter, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(INVALID_REQUEST);
        problem.setTitle("Invalid request");
        problem.setProperty("parameter", parameter);
        return problem;
    }
}
