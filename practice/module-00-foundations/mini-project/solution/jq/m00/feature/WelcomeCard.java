package jq.m00.feature;

public class WelcomeCard {

    public static String border() {
        return "+---------------------------+";
    }

    public static void main(String[] args) {
        System.out.println(border());
        System.out.println("| Welcome to Spoke, rider!  |");
        System.out.println("| Your first 30 minutes are |");
        System.out.println("| free. Enjoy the ride.     |");
        System.out.println(border());
    }
}
