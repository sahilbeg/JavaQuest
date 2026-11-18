package jq.m10.incident;

/** What reserve answers: reserved, or refused with a short reason such as "dock-taken". */
public record ReservationResult(boolean reserved, String reason) {

    public static ReservationResult ok() {
        return new ReservationResult(true, "reserved");
    }

    public static ReservationResult refused(String reason) {
        return new ReservationResult(false, reason);
    }
}
