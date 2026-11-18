package jq.m04.dppatterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlanningDeskTest {

    @Test
    @DisplayName("The promo team reaches the most riders without visiting two neighbouring stations")
    void takeOrSkip() {
        assertEquals(10, PlanningDesk.maxRidersWithoutNeighbours(new int[] {5, 1, 1, 5}),
                "Riders {5, 1, 1, 5}: visit the first and last stations for 5 + 5 = 10");
        assertEquals(12, PlanningDesk.maxRidersWithoutNeighbours(new int[] {2, 7, 9, 3, 1}),
                "Riders {2, 7, 9, 3, 1}: visit stations 0, 2 and 4 for 2 + 9 + 1 = 12");
        assertEquals(4, PlanningDesk.maxRidersWithoutNeighbours(new int[] {2, 1, 1, 2}),
                "Riders {2, 1, 1, 2}: the best is 2 + 2 = 4, which skips TWO stations in the middle."
                        + " Adding up every other station only gives 3");
    }

    @Test
    @DisplayName("No stations reach nobody, and one station is visited on its own")
    void takeOrSkipEdges() {
        assertEquals(4, PlanningDesk.maxRidersWithoutNeighbours(new int[] {4}),
                "One station with 4 riders: visit it and reach 4");
        assertEquals(0, PlanningDesk.maxRidersWithoutNeighbours(new int[0]),
                "No stations means 0 riders reached");
        assertEquals(0, PlanningDesk.maxRidersWithoutNeighbours(new int[] {0, 0, 0}),
                "Three empty stations reach 0 riders");
    }

    @Test
    @DisplayName("fewestCoins beats the greedy answer when coins are 1, 3 and 4")
    void fewestCoinsBeatsGreedy() {
        assertEquals(2, PlanningDesk.fewestCoins(new int[] {1, 3, 4}, 6),
                "6 cents with coins 1, 3, 4: 3 + 3 is 2 coins. Taking the biggest coin first gives 4 + 1 + 1, which is 3");
        assertEquals(6, PlanningDesk.fewestCoins(new int[] {1, 5, 10, 25}, 63),
                "63 cents with coins 1, 5, 10, 25 needs 6 coins: 25 + 25 + 10 + 1 + 1 + 1");
    }

    @Test
    @DisplayName("fewestCoins returns -1 when the amount cannot be paid and 0 for nothing")
    void fewestCoinsEdges() {
        assertEquals(-1, PlanningDesk.fewestCoins(new int[] {5, 10}, 3),
                "3 cents cannot be made from 5 and 10 cent coins, so the answer is -1");
        assertEquals(-1, PlanningDesk.fewestCoins(new int[] {2}, 7),
                "7 cents cannot be made from 2 cent coins only, so the answer is -1");
        assertEquals(0, PlanningDesk.fewestCoins(new int[] {1, 3, 4}, 0),
                "Paying 0 cents needs 0 coins");
    }

    @Test
    @DisplayName("waysToPay counts each mix of coins once, whatever order the coins go in")
    void waysToPay() {
        assertEquals(4L, PlanningDesk.waysToPay(new int[] {1, 2, 5}, 5),
                "5 cents with coins 1, 2, 5 has 4 mixes: 5, 2+2+1, 2+1+1+1 and 1+1+1+1+1");
        assertEquals(2L, PlanningDesk.waysToPay(new int[] {1, 2}, 3),
                "3 cents with coins 1 and 2 has 2 mixes: 1+1+1 and 1+2. 2+1 is the same mix as 1+2."
                        + " Put the coins in the OUTER loop");
    }

    @Test
    @DisplayName("waysToPay gives 1 way to pay nothing and 0 ways for an impossible amount")
    void waysToPayEdges() {
        assertEquals(1L, PlanningDesk.waysToPay(new int[] {1, 2, 5}, 0),
                "There is exactly 1 way to pay 0 cents: use no coins. This is the base case ways[0] = 1");
        assertEquals(0L, PlanningDesk.waysToPay(new int[] {2}, 3),
                "3 cents cannot be made from 2 cent coins, so there are 0 ways");
    }

    @Test
    @DisplayName("Routes through an open grid and around a closed square are counted")
    void gridRoutes() {
        assertEquals(6L, PlanningDesk.routesThroughGrid(new boolean[3][3]),
                "An open 3 by 3 grid has 6 routes that only move right or down");
        boolean[][] middleClosed = new boolean[3][3];
        middleClosed[1][1] = true;
        assertEquals(2L, PlanningDesk.routesThroughGrid(middleClosed),
                "With the middle square closed, only 2 routes are left: along the top edge or along the left edge");
    }

    @Test
    @DisplayName("A blocked start, a blocked end or a wall leaves no routes")
    void gridEdges() {
        assertEquals(1L, PlanningDesk.routesThroughGrid(new boolean[1][1]),
                "A 1 by 1 open grid has 1 route: you are already there");
        boolean[][] startClosed = new boolean[2][2];
        startClosed[0][0] = true;
        assertEquals(0L, PlanningDesk.routesThroughGrid(startClosed),
                "When the start square is closed there are 0 routes");
        boolean[][] endClosed = new boolean[2][2];
        endClosed[1][1] = true;
        assertEquals(0L, PlanningDesk.routesThroughGrid(endClosed),
                "When the end square is closed there are 0 routes");
        boolean[][] wall = new boolean[1][4];
        wall[0][2] = true;
        assertEquals(0L, PlanningDesk.routesThroughGrid(wall),
                "A single street with a closed square in it has 0 routes: nothing can get past the block");
        assertEquals(0L, PlanningDesk.routesThroughGrid(new boolean[0][0]),
                "An empty grid has 0 routes");
    }

    @Test
    @DisplayName("A 30 by 30 grid is counted fast, with a long")
    void bigGrid() {
        long routes = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> PlanningDesk.routesThroughGrid(new boolean[30][30]),
                "routesThroughGrid took over 2 seconds. Plain recursion repeats work; fill a table instead");
        assertEquals(30067266499541040L, routes,
                "An open 30 by 30 grid has 30067266499541040 routes. That is too big for an int, so use long");
    }

    @Test
    @DisplayName("The truck carries the most valuable crates that fit")
    void truckLoad() {
        assertEquals(9, PlanningDesk.bestTruckLoad(new int[] {1, 3, 4, 5}, new int[] {1, 4, 5, 7}, 7),
                "Capacity 7: the crates weighing 3 and 4 fit together and are worth 4 + 5 = 9");
        assertEquals(3, PlanningDesk.bestTruckLoad(new int[] {2}, new int[] {3}, 6),
                "One crate weighing 2 and worth 3 can go on the truck only once, so the best is 3, not 9."
                        + " Loop the capacity DOWNWARD");
    }

    @Test
    @DisplayName("An empty truck, no crates or crates that are too heavy give 0")
    void truckLoadEdges() {
        assertEquals(0, PlanningDesk.bestTruckLoad(new int[] {4, 5}, new int[] {10, 20}, 0),
                "A truck with capacity 0 carries nothing, so the value is 0");
        assertEquals(0, PlanningDesk.bestTruckLoad(new int[0], new int[0], 10),
                "With no crates, the value is 0");
        assertEquals(0, PlanningDesk.bestTruckLoad(new int[] {8, 9}, new int[] {10, 20}, 7),
                "Every crate is heavier than 7, so nothing fits and the value is 0");
    }

    @Test
    @DisplayName("editDistance counts the fewest single-letter changes between two station names")
    void editDistance() {
        assertEquals(1, PlanningDesk.editDistance("Harbour", "Harbor"),
                "Harbour to Harbor: delete the u, 1 edit");
        assertEquals(2, PlanningDesk.editDistance("Uptwon", "Uptown"),
                "Uptwon to Uptown: replace w with o and o with w, 2 edits");
        assertEquals(3, PlanningDesk.editDistance("Old Mill", "Oldmil"),
                "Old Mill to Oldmil: delete the space, replace M with m, delete one l, 3 edits");
        assertEquals(5, PlanningDesk.editDistance("", "Depot"),
                "From empty text to Depot: insert 5 letters");
        assertEquals(5, PlanningDesk.editDistance("Depot", ""),
                "From Depot to empty text: delete 5 letters");
        assertEquals(0, PlanningDesk.editDistance("Old Mill", "Old Mill"),
                "The same name needs 0 edits");
    }

    @Test
    @DisplayName("Big inputs come back fast: 10000 cents and two 1500-letter names")
    void bigInputsAreFast() {
        int coins = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> PlanningDesk.fewestCoins(new int[] {7, 13, 1}, 10000),
                "fewestCoins took over 2 seconds. Fill a table from 0 up to the amount");
        assertEquals(772, coins, "10000 cents with coins 1, 7 and 13 needs 772 coins");
        String a = "ab".repeat(750);
        String b = "ba".repeat(750);
        int edits = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> PlanningDesk.editDistance(a, b),
                "editDistance took over 2 seconds. Use a table with one cell per pair of prefixes");
        assertEquals(2, edits, "abab...ab to baba...ba is 2 edits: delete the first a and add an a at the end");
    }
}
