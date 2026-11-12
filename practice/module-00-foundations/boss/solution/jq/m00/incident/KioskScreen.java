package jq.m00.incident;

public class KioskScreen {

    public static String welcome() {
        return "Welcome to Spoke!";
    }

    public static void main(String[] args) {
        System.out.println(welcome());
        System.out.println("Tap your card to unlock a bike.");
        System.out.println("Riverside Station");
    }
}
