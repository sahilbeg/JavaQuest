package jq.m01.consoleio;

import java.util.Scanner;

public class KioskPrompt {

    public static int readMinutes(Scanner in) {
        return in.nextInt();
    }

    public static String readStation(Scanner in) {
        return in.nextLine().trim();
    }

    public static void run(Scanner in) {
        System.out.println("How many minutes did you ride?");
        int minutes = readMinutes(in);
        System.out.println("Fare: $" + (1.00 + minutes * 0.15));
    }

    public static void main(String[] args) {
        run(new Scanner(System.in));
    }
}
