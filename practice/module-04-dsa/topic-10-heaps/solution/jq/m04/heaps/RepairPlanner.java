package jq.m04.heaps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class RepairPlanner {

    public record Repair(String bikeId, int urgency) {
    }

    public static List<String> order(List<Repair> repairs) {
        Comparator<Repair> mostUrgentFirst = Comparator.comparingInt(Repair::urgency)
                .thenComparing(Repair::bikeId);
        PriorityQueue<Repair> queue = new PriorityQueue<>(mostUrgentFirst);
        queue.addAll(repairs);
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            result.add(queue.poll().bikeId());
        }
        return result;
    }

    public static void main(String[] args) {
        List<Repair> reports = List.of(
                new Repair("B-1042", 3),
                new Repair("B-1017", 1),
                new Repair("B-1088", 2),
                new Repair("B-1003", 1));
        System.out.println("Fix in this order: " + order(reports));
    }
}
