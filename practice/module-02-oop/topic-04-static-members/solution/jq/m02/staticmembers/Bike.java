package jq.m02.staticmembers;

public class Bike {

    private static int counter = 0;
    private final String id;
    private final String type;

    public Bike(String type) {
        this.type = type;
        this.id = nextId();
    }

    private static String nextId() {
        counter++;
        return "B-" + (1000 + counter);
    }

    public static int created() {
        return counter;
    }

    public static void resetCounter() {
        counter = 0;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
