package jq.m07.feature;

import java.io.PrintStream;

/** The kiosk's command line: java -jar kiosk.jar 12 prints the fare for a 12 minute ride. */
public final class Kiosk {

    static final String USAGE = "Usage: java -jar kiosk.jar <minutes> | --version";
    static final long UNLOCK_CENTS = 100;
    static final long CENTS_PER_MINUTE = 15;

    private Kiosk() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    /** Handles one command and returns the exit code: 0 when it worked, 2 when the arguments are wrong. */
    public static int run(String[] args, PrintStream out, PrintStream err) {
        // TODO 1: with one argument, "--version", print "Spoke kiosk " + BuildInfo.version() and return 0.
        // TODO 2: with one argument that is a whole number of minutes, 0 or more, print the fare and return 0.
        //         The fare is UNLOCK_CENTS + minutes * CENTS_PER_MINUTE. For 12: "Fare for 12 min: $2.80". Use dollars(cents).
        // TODO 3: for anything else (no arguments, two arguments, "abc", "-5"), print USAGE to err and return 2.
        return 0;
    }

    static String dollars(long cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
