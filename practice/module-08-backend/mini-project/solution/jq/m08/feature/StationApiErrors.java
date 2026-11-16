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

    /** GET /api/v1/stations/moon: no such station. Spring fills in instance with the path. */
    @ExceptionHandler(StationNotFoundException.class)
    public ProblemDetail stationNotFound(StationNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setType(STATION_NOT_FOUND);
        problem.setTitle("Station not found");
        problem.setProperty("stationId", e.getStationId());
        return problem;
    }

    /** ?minBikes=abc or ?type=SCOOTER: Spring could not turn the text into an int or a BikeType. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail wrongType(MethodArgumentTypeMismatchException e) {
        return invalid(e.getName(), e.getName() + " cannot be " + e.getValue());
    }

    /** ?minBikes=13: the value is a number, but it breaks @Min(0) or @Max(12). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail outOfRange(HandlerMethodValidationException e) {
        ParameterValidationResult result = e.getParameterValidationResults().get(0);
        String name = result.getMethodParameter().getParameterName();
        MessageSourceResolvable error = result.getResolvableErrors().get(0);
        return invalid(name, name + " " + error.getDefaultMessage());
    }

    private static ProblemDetail invalid(String parameter, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(INVALID_REQUEST);
        problem.setTitle("Invalid request");
        problem.setProperty("parameter", parameter);
        return problem;
    }
}
