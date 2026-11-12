package jq.m01.forloops;

public class DockList {

    public static String dockLine(int docks) {
        String line = "";
        for (int i = 1; i <= docks; i++) {
            if (i > 1) {
                line = line + " ";
            }
            line = line + i;
        }
        return line;
    }

    public static String evenDocks(int docks) {
        String line = "";
        for (int i = 2; i <= docks; i += 2) {
            if (i > 2) {
                line = line + " ";
            }
            line = line + i;
        }
        return line;
    }

    public static double totalClassicFare(int rides, int minutesEach) {
        double total = 0.0;
        for (int ride = 1; ride <= rides; ride++) {
            total = total + 1.00 + minutesEach * 0.15;
        }
        return total;
    }

    public static int firstFreeDock(int docks, int takenUpTo) {
        int free = -1;
        for (int i = 1; i <= docks; i++) {
            if (i > takenUpTo) {
                free = i;
                break;
            }
        }
        return free;
    }

    public static void main(String[] args) {
        System.out.println("Docks: " + dockLine(12));
        System.out.println("Even docks: " + evenDocks(12));
        System.out.println("3 rides of 10 minutes: $" + totalClassicFare(3, 10));
        System.out.println("First free dock: " + firstFreeDock(12, 7));
    }
}
