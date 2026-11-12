package jq.m04.feature;

public record Station(String name, int bikes, int target) {

    public int surplus() {
        return bikes - target;
    }
}
