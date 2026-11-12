package jq.m01.forloops;

public class DockList {

    public static String dockLine(int docks) {
        // TODO 1: use a for loop from 1 to docks to build "1 2 3 ... N" with one space between numbers and none at the end
        return "";
    }

    public static String evenDocks(int docks) {
        // TODO 2: use a for loop that starts at 2 and steps by 2 to build "2 4 6 ..." the same way
        return "";
    }

    public static double totalClassicFare(int rides, int minutesEach) {
        // TODO 3: use a for loop that runs once per ride and adds 1.00 + minutesEach * 0.15 to a total
        return 0.0;
    }

    public static int firstFreeDock(int docks, int takenUpTo) {
        // TODO 4: loop from 1 to docks, and break when you find the first dock number greater than takenUpTo
        // TODO 5: return that dock number, or -1 if the loop never found one
        return 0;
    }

    public static void main(String[] args) {
        System.out.println("Docks: " + dockLine(12));
        System.out.println("Even docks: " + evenDocks(12));
        System.out.println("3 rides of 10 minutes: $" + totalClassicFare(3, 10));
        System.out.println("First free dock: " + firstFreeDock(12, 7));
    }
}
