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
        if (args.length != 1) {
            err.println(USAGE);
            return 2;
        }
        if (args[0].equals("--version")) {
            out.println("Spoke kiosk " + BuildInfo.version());
            return 0;
        }
        int minutes;
        try {
            minutes = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            err.println(USAGE);
            return 2;
        }
        if (minutes < 0) {
            err.println(USAGE);
            return 2;
        }
        long cents = UNLOCK_CENTS + minutes * CENTS_PER_MINUTE;
        out.println("Fare for " + minutes + " min: " + dollars(cents));
        return 0;
    }

    static String dollars(long cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
