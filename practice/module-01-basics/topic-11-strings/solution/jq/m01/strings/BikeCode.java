package jq.m01.strings;

public class BikeCode {

    public static String normalize(String raw) {
        String clean = raw.trim().toUpperCase();
        if (clean.startsWith("B") && !clean.startsWith("B-")) {
            clean = "B-" + clean.substring(1);
        }
        return clean;
    }

    public static boolean isValid(String code) {
        if (code.length() != 6 || !code.startsWith("B-")) {
            return false;
        }
        for (int i = 2; i < code.length(); i++) {
            if (!Character.isDigit(code.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String number(String code) {
        return code.substring(2);
    }

    public static void main(String[] args) {
        String[] typed = {" b-1042 ", "B1042", "b-10x2"};
        for (int i = 0; i < typed.length; i++) {
            String code = normalize(typed[i]);
            System.out.println("[" + typed[i] + "] -> " + code + " valid: " + isValid(code));
        }
    }
}
