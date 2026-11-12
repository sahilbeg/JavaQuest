package jq.m04.recursion;

public class Recursion {

    public static long factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    public static int sum(int[] docks) {
        return sumFrom(docks, 0);
    }

    private static int sumFrom(int[] docks, int index) {
        if (index == docks.length) {
            return 0;
        }
        return docks[index] + sumFrom(docks, index + 1);
    }

    public static String countDown(int n) {
        if (n <= 0) {
            return "go";
        }
        return n + " " + countDown(n - 1);
    }

    public static String reverse(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return reverse(s.substring(1)) + s.charAt(0);
    }

    public static int power(int base, int exp) {
        if (exp == 0) {
            return 1;
        }
        return base * power(base, exp - 1);
    }
}
