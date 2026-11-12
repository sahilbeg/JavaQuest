package jq.m00.incident;

public class KioskScreen {

    public static String welcome() {
        // TODO bronze: riders must see exactly "Welcome to Spoke!"
        return "welcome to spoke.";
    }

    public static void main(String[] args) {
        // TODO silver: the welcome and the tap instruction belong on separate lines
        System.out.print(welcome());
        System.out.print("Tap your card to unlock a bike.");
        // TODO gold: print the station name "Riverside Station" as the third line
    }
}
