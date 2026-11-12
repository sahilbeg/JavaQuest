package jq.m04.recursion;

public class Recursion {

    public static long factorial(int n) {
        // TODO 1: base case: if n is 1 or less, return 1
        // TODO 2: recursive case: return n times factorial(n - 1)
        return 0;
    }

    public static int sum(int[] docks) {
        return sumFrom(docks, 0);
    }

    private static int sumFrom(int[] docks, int index) {
        // TODO 3: base case: when index reaches docks.length, return 0
        // TODO 4: recursive case: return docks[index] plus sumFrom(docks, index + 1)
        return 0;
    }

    public static String countDown(int n) {
        // TODO 5: return "go" when n is 0 or less, otherwise n + " " + countDown(n - 1)
        return "";
    }

    public static String reverse(String s) {
        // TODO 6: return s when it is empty, otherwise reverse(s.substring(1)) + s.charAt(0)
        return "";
    }

    public static int power(int base, int exp) {
        // TODO 7: return 1 when exp is 0, otherwise base * power(base, exp - 1)
        return 0;
    }
}
