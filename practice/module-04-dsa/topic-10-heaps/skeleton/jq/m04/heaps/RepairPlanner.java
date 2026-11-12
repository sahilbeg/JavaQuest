package jq.m04.heaps;

import java.util.ArrayList;
import java.util.List;

public class RepairPlanner {

    public record Repair(String bikeId, int urgency) {
    }

    public static List<String> order(List<Repair> repairs) {
        // TODO 6: build a Comparator<Repair>: urgency first (lower is more urgent), then bikeId.
        // TODO 7: put every repair in a java.util.PriorityQueue with that comparator,
        // then poll until it is empty and collect each bikeId.
        return new ArrayList<>();
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
