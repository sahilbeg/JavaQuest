package jq.m04.bigo;

public class StepCounter {

    public static int countLinear(int n) {
        int steps = 0;
        for (int i = 0; i < n; i++) {
            steps++;
        }
        return steps;
    }

    public static int countNested(int n) {
        int steps = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                steps++;
            }
        }
        return steps;
    }

    public static int countHalving(int n) {
        int steps = 0;
        while (n > 1) {
            n = n / 2;
            steps++;
        }
        return steps;
    }

    public static String bigO(String name) {
        switch (name) {
            case "linear":
                return "O(n)";
            case "nested":
                return "O(n^2)";
            case "halving":
                return "O(log n)";
            case "hashset":
                return "O(1)";
            default:
                return "";
        }
    }
}
