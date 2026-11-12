package jq.m03.generics;

public class Pair<A, B> {

    private A first;
    private B second;

    public Pair(A first, B second) {
        // TODO 6: store first and second in the fields
    }

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }
}
