package jq.m01.consoleio;

import java.util.Scanner;

public class KioskPrompt {

    public static int readMinutes(Scanner in) {
        // TODO 1: read a whole number with in.nextInt() and return it
        return 0;
    }

    public static String readStation(Scanner in) {
        // TODO 2: read a whole line with in.nextLine(), trim the spaces, and return it
        return "";
    }

    public static void run(Scanner in) {
        // TODO 3: print "How many minutes did you ride?" with println
        // TODO 4: read the minutes with readMinutes(in)
        // TODO 5: print "Fare: $" joined with 1.00 + minutes * 0.15
    }

    public static void main(String[] args) {
        run(new Scanner(System.in));
    }
}
