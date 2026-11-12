package jq.m04.dp;

public class Passes {

    public static long fib(int n) {
        // TODO 1: fill a long[] table from the bottom up: table[0] = 0, table[1] = 1,
        //         table[i] = table[i - 1] + table[i - 2]. Return table[n].
        return 0;
    }

    public static int cheapestCover(int[] rideDays, int dayPrice, int weekPrice, int monthPrice) {
        // TODO 2: return 0 when there are no ride days.
        // TODO 3: build cost[d] = cheapest cover for every ride day up to day d.
        //         On a day with no ride, cost[d] = cost[d - 1].
        //         On a ride day, take the smallest of a day pass, a week pass and a month pass.
        return -1;
    }

    public static int waysToDock(int bikes) {
        // TODO 4: ways[0] = 1, ways[1] = 1, ways[b] = ways[b - 1] + ways[b - 2].
        return 0;
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
