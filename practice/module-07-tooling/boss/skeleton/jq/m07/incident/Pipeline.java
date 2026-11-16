package jq.m07.incident;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Runs the three pipeline stages the way the nightly build does, and prints GREEN or RED for each. */
public final class Pipeline {

    private Pipeline() {
    }

    public static void main(String[] args) {
        stage("release gate", () -> ReleaseGate.canShip("1.9.3", "1.10.0"));
        stage("load config", () -> KioskConfig.load(Path.of("ci-workspace", "config")).equals(KioskConfig.defaults()));
        stage("night fare", () -> new NightFare(at("02:00")).surchargeCents() == NightFare.SURCHARGE_CENTS
                && new NightFare(at("12:00")).surchargeCents() == 0);
    }

    /** A clock that is always at this time of day, in UTC, like the CI machine. */
    private static Clock at(String time) {
        return Clock.fixed(Instant.parse("2026-11-16T" + time + ":00Z"), ZoneOffset.UTC);
    }

    private interface Check {
        boolean passes() throws Exception;
    }

    private static void stage(String name, Check check) {
        String result;
        try {
            result = check.passes() ? "GREEN" : "RED";
        } catch (Exception e) {
            result = "RED (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
        }
        System.out.println(name + ": " + result);
    }
}
