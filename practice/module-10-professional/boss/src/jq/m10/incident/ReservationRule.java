package jq.m10.incident;

import java.util.Optional;

/** One business rule. It returns a reason to refuse the request, or Optional.empty() to allow it. */
@FunctionalInterface
public interface ReservationRule {
    Optional<String> check(ReservationRequest request);
}
