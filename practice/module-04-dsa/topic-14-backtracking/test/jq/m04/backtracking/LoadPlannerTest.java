package jq.m04.backtracking;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoadPlannerTest {

    private static final Duration FAST = Duration.ofSeconds(2);

    @Test
    @DisplayName("promoSets lists all 8 sets of three promos, in the stated order")
    void promoSetsOfThree() {
        List<List<String>> sets = assertTimeoutPreemptively(FAST,
                () -> LoadPlanner.promoSets(List.of("WEEKEND", "STUDENT", "FIRSTRIDE")),
                "promoSets should finish at once for three promos");
        assertEquals(List.of(
                        List.of(),
                        List.of("WEEKEND"),
                        List.of("WEEKEND", "STUDENT"),
                        List.of("WEEKEND", "STUDENT", "FIRSTRIDE"),
                        List.of("WEEKEND", "FIRSTRIDE"),
                        List.of("STUDENT"),
                        List.of("STUDENT", "FIRSTRIDE"),
                        List.of("FIRSTRIDE")),
                sets,
                "Record the current set first, then try each later promo in turn. If every set comes back empty,"
                        + " you added the same list object each time: add new ArrayList<>(current) instead");
    }

    @Test
    @DisplayName("promoSets handles no promos, one promo and ten promos")
    void promoSetsSizes() {
        assertEquals(List.of(List.of()), LoadPlanner.promoSets(List.of()),
                "With no promos there is exactly one set: the empty set");
        assertEquals(List.of(List.of(), List.of("STUDENT")), LoadPlanner.promoSets(List.of("STUDENT")),
                "With one promo there are two sets: without it and with it");
        List<String> ten = List.of("P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9");
        List<List<String>> sets = assertTimeoutPreemptively(FAST, () -> LoadPlanner.promoSets(ten),
                "promoSets should finish fast for ten promos");
        assertEquals(1024, sets.size(), "Ten promos have 2^10 = 1024 sets, because each promo is in or out");
        assertEquals(1024, new HashSet<>(sets).size(), "All 1024 sets should be different from each other");
    }

    @Test
    @DisplayName("promoSets records a separate copy of each set, not the same list again and again")
    void promoSetsCopies() {
        List<List<String>> sets = LoadPlanner.promoSets(List.of("WEEKEND", "STUDENT"));
        assertEquals(List.of(List.of(), List.of("WEEKEND"), List.of("WEEKEND", "STUDENT"), List.of("STUDENT")), sets,
                "Two promos give [], [WEEKEND], [WEEKEND, STUDENT], [STUDENT]");
        for (int i = 0; i < sets.size(); i++) {
            for (int j = i + 1; j < sets.size(); j++) {
                assertNotSame(sets.get(i), sets.get(j),
                        "Each recorded set must be its own list. Record new ArrayList<>(current), not current itself");
            }
        }
    }

    @Test
    @DisplayName("visitOrders lists all 6 orders of three stations, in the stated order")
    void visitOrdersOfThree() {
        List<List<String>> orders = assertTimeoutPreemptively(FAST,
                () -> LoadPlanner.visitOrders(List.of("Harbor", "Uptown", "Depot")),
                "visitOrders should finish at once for three stations");
        assertEquals(List.of(
                        List.of("Harbor", "Uptown", "Depot"),
                        List.of("Harbor", "Depot", "Uptown"),
                        List.of("Uptown", "Harbor", "Depot"),
                        List.of("Uptown", "Depot", "Harbor"),
                        List.of("Depot", "Harbor", "Uptown"),
                        List.of("Depot", "Uptown", "Harbor")),
                orders,
                "Try the stations in their given order at each position, skipping ones already used."
                        + " Remember to un-choose: remove the station and mark it unused again");
    }

    @Test
    @DisplayName("visitOrders gives 24 different orders for four stations, and one empty order for none")
    void visitOrdersOfFour() {
        List<List<String>> orders = assertTimeoutPreemptively(FAST,
                () -> LoadPlanner.visitOrders(List.of("Riverside", "Old Mill", "Harbor", "Depot")),
                "visitOrders should finish at once for four stations");
        assertEquals(24, orders.size(), "Four stations can be visited in 4 x 3 x 2 x 1 = 24 orders");
        assertEquals(24, new HashSet<>(orders).size(), "All 24 orders should be different");
        assertEquals(List.of("Riverside", "Old Mill", "Harbor", "Depot"), orders.get(0),
                "The first order keeps the stations as given");
        assertEquals(List.of("Riverside", "Old Mill", "Depot", "Harbor"), orders.get(1),
                "The second order swaps only the last two stations");
        assertEquals(List.of("Depot", "Harbor", "Old Mill", "Riverside"), orders.get(23),
                "The last order is the given order reversed");
        assertEquals(List.of(List.of()), LoadPlanner.visitOrders(List.of()),
                "With no stations there is exactly one order: the empty one");
    }

    @Test
    @DisplayName("loadsForCapacity finds every load that fills the truck exactly")
    void loadsBasic() {
        assertEquals(List.of(List.of(2, 2, 3), List.of(2, 5)),
                LoadPlanner.loadsForCapacity(new int[] {2, 3, 2, 5, 3}, 7),
                "Bikes taking {2, 3, 2, 5, 3} slots fill 7 slots as [2, 2, 3] or [2, 5]. Each bike is used at most once");
        assertEquals(List.of(List.of(1, 1, 3), List.of(1, 2, 2), List.of(1, 4), List.of(2, 3)),
                LoadPlanner.loadsForCapacity(new int[] {4, 1, 3, 2, 1, 2}, 5),
                "Each load is sorted small to large, and the loads are in dictionary order");
    }

    @Test
    @DisplayName("loadsForCapacity never lists the same load twice when bikes take the same space")
    void loadsSkipDuplicates() {
        assertEquals(List.of(List.of(2, 2)), LoadPlanner.loadsForCapacity(new int[] {2, 2, 2, 2, 2, 2}, 4),
                "Six bikes of 2 slots make one load [2, 2], not 15 copies of it. After sorting, skip sorted[i]"
                        + " when i > start and it equals sorted[i - 1]");
        assertEquals(List.of(List.of(1, 2), List.of(3)), LoadPlanner.loadsForCapacity(new int[] {3, 1, 2, 1, 2}, 3),
                "{3, 1, 2, 1, 2} fill 3 slots as [1, 2] or [3]. [1, 2] should appear once");
    }

    @Test
    @DisplayName("loadsForCapacity returns an empty list when nothing fits, and leaves the caller's array alone")
    void loadsEdges() {
        int[] bikes = {5, 2, 3};
        assertEquals(List.of(List.of(2, 3), List.of(5)), LoadPlanner.loadsForCapacity(bikes, 5),
                "{5, 2, 3} fill 5 slots as [2, 3] or [5]");
        assertArrayEquals(new int[] {5, 2, 3}, bikes,
                "Sort a copy (bikeSlots.clone()), not the caller's array. It should still be {5, 2, 3}");
        assertEquals(List.of(), LoadPlanner.loadsForCapacity(new int[] {4, 6}, 3),
                "No bike fits in 3 slots, so there are no loads: return an empty list");
        assertEquals(List.of(), LoadPlanner.loadsForCapacity(new int[0], 5),
                "With no bikes there are no loads: return an empty list");
    }

    @Test
    @DisplayName("loadsForCapacity prunes, so 30 bikes are planned at once")
    void loadsPrune() {
        int[] bikes = new int[30];
        for (int i = 0; i < 30; i++) {
            bikes[i] = i + 1;
        }
        List<List<Integer>> loads = assertTimeoutPreemptively(FAST, () -> LoadPlanner.loadsForCapacity(bikes, 10),
                "30 bikes have about a billion subsets. Stop a branch as soon as a bike is bigger than the space left");
        assertEquals(10, loads.size(), "Bikes of 1 to 30 slots fill 10 slots in exactly 10 different ways");
        assertEquals(List.of(1, 2, 3, 4), loads.get(0), "The first load in dictionary order is [1, 2, 3, 4]");
        assertEquals(List.of(10), loads.get(9), "The last load is the single 10-slot bike, [10]");
    }

    @Test
    @DisplayName("refillPlans may use a crate size again and again")
    void refillBasic() {
        assertEquals(List.of(List.of(2, 2, 2, 2), List.of(2, 3, 3), List.of(3, 5)),
                LoadPlanner.refillPlans(new int[] {5, 2, 3}, 8),
                "Crates of 5, 2 and 3 bikes make 8 as [2, 2, 2, 2], [2, 3, 3] or [3, 5]. Explore with i, not i + 1");
        assertEquals(List.of(List.of(2, 2, 3), List.of(7)), LoadPlanner.refillPlans(new int[] {2, 3, 6, 7}, 7),
                "Crates of 2, 3, 6 and 7 make 7 as [2, 2, 3] or [7]");
    }

    @Test
    @DisplayName("refillPlans handles one crate size and targets nothing can reach")
    void refillEdges() {
        assertEquals(List.of(List.of(3, 3, 3)), LoadPlanner.refillPlans(new int[] {3}, 9),
                "One crate size of 3 makes 9 only as [3, 3, 3]");
        assertEquals(List.of(), LoadPlanner.refillPlans(new int[] {4, 6}, 7),
                "4s and 6s only make even numbers, so 7 has no plan: return an empty list");
    }
}
