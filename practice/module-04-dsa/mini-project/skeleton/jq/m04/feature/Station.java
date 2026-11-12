package jq.m04.feature;

public record Station(String name, int bikes, int target) {

    public int surplus() {
        // TODO 1: bikes minus target
        return 0;
    }
}
