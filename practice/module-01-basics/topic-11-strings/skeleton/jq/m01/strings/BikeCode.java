package jq.m01.strings;

public class BikeCode {

    public static String normalize(String raw) {
        // TODO 1: trim the spaces and make the text upper case
        // TODO 2: if it starts with "B" but not "B-", put a dash after the B
        return "";
    }

    public static boolean isValid(String code) {
        // TODO 3: return false unless the length is 6 and it starts with "B-"
        // TODO 4: loop over indexes 2 to 5 and return false if Character.isDigit says no
        return false;
    }

    public static String number(String code) {
        // TODO 5: return the 4 digits after "B-" using substring
        return "";
    }

    public static void main(String[] args) {
        String[] typed = {" b-1042 ", "B1042", "b-10x2"};
        for (int i = 0; i < typed.length; i++) {
            String code = normalize(typed[i]);
            System.out.println("[" + typed[i] + "] -> " + code + " valid: " + isValid(code));
        }
    }
}
