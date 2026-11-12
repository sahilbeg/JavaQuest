package jq.m01.whileloops;

public class CardReader {

    public static int tapsNeeded(int firstSuccessfulTap, int maxTaps) {
        int tap = 1;
        while (tap <= maxTaps) {
            if (tap == firstSuccessfulTap) {
                return tap;
            }
            tap++;
        }
        return -1;
    }

    public static int minutesUntilFull(int freeDocks, int bikesArrivingPerMinute) {
        if (bikesArrivingPerMinute <= 0) {
            return -1;
        }
        int minutes = 0;
        while (freeDocks > 0) {
            freeDocks = freeDocks - bikesArrivingPerMinute;
            minutes++;
        }
        return minutes;
    }

    public static String countdown(int from) {
        String text = "";
        int n = from;
        while (n >= 1) {
            text = text + n + " ";
            n--;
        }
        return text + "go";
    }

    public static void main(String[] args) {
        System.out.println("Taps used: " + tapsNeeded(2, 3));
        System.out.println("Minutes until Harbor is full: " + minutesUntilFull(12, 5));
        System.out.println(countdown(3));
    }
}
