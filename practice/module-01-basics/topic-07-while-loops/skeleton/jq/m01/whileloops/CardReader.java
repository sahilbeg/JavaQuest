package jq.m01.whileloops;

public class CardReader {

    public static int tapsNeeded(int firstSuccessfulTap, int maxTaps) {
        // TODO 1: start a counter at 1 and use a while loop that runs while the counter is at most maxTaps
        // TODO 2: inside the loop, return the counter when it equals firstSuccessfulTap, then add 1 to it
        // TODO 3: after the loop, return -1 because the card never worked
        return 0;
    }

    public static int minutesUntilFull(int freeDocks, int bikesArrivingPerMinute) {
        // TODO 4: return -1 if bikesArrivingPerMinute is 0 or less, so the loop can never run forever
        // TODO 5: while freeDocks is above 0, take away the arriving bikes and add 1 to a minutes counter
        return 0;
    }

    public static String countdown(int from) {
        // TODO 6: build text like "3 2 1 go" with a while loop that counts down from "from" to 1
        return "";
    }

    public static void main(String[] args) {
        System.out.println("Taps used: " + tapsNeeded(2, 3));
        System.out.println("Minutes until Harbor is full: " + minutesUntilFull(12, 5));
        System.out.println(countdown(3));
    }
}
