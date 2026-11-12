package jq.m04.dp;

public class Passes {

    public static long fib(int n) {
        if (n < 2) {
            return n;
        }
        long[] table = new long[n + 1];
        table[1] = 1;
        for (int i = 2; i <= n; i++) {
            table[i] = table[i - 1] + table[i - 2];
        }
        return table[n];
    }

    public static int cheapestCover(int[] rideDays, int dayPrice, int weekPrice, int monthPrice) {
        if (rideDays.length == 0) {
            return 0;
        }
        int lastDay = rideDays[rideDays.length - 1];
        boolean[] rides = new boolean[lastDay + 1];
        for (int day : rideDays) {
            rides[day] = true;
        }
        int[] cost = new int[lastDay + 1];
        for (int d = 1; d <= lastDay; d++) {
            if (!rides[d]) {
                cost[d] = cost[d - 1];
                continue;
            }
            int withDay = cost[d - 1] + dayPrice;
            int withWeek = cost[Math.max(0, d - 7)] + weekPrice;
            int withMonth = cost[Math.max(0, d - 30)] + monthPrice;
            cost[d] = Math.min(withDay, Math.min(withWeek, withMonth));
        }
        return cost[lastDay];
    }

    public static int waysToDock(int bikes) {
        int[] ways = new int[bikes + 2];
        ways[0] = 1;
        ways[1] = 1;
        for (int b = 2; b <= bikes; b++) {
            ways[b] = ways[b - 1] + ways[b - 2];
        }
        return ways[bikes];
    }

    public static void main(String[] args) {
        int[] month = new int[30];
        for (int i = 0; i < 30; i++) {
            month[i] = i + 1;
        }
        System.out.println("fib(80) = " + fib(80));
        System.out.println("Cheapest cover for 30 days in a row: $" + cheapestCover(month, 3, 10, 25));
        System.out.println("Ways to dock 5 bikes: " + waysToDock(5));
    }
}
