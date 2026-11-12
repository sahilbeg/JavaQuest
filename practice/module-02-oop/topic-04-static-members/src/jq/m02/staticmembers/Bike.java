package jq.m02.staticmembers;

public class Bike {

    private static int counter = 0;
    private String id;
    private String type;

    public Bike(String type) {
        // TODO 4: store the type in this.type and set this.id = nextId();
    }

    private static String nextId() {
        // TODO 3: add 1 to counter, then return "B-" followed by 1000 + counter
        return "";
    }

    public static int created() {
        // TODO 5: return the shared counter
        return 0;
    }

    public static void resetCounter() {
        counter = 0;
    }

    public String getId() {
        // TODO 5: return the id field
        return "";
    }

    public String getType() {
        // TODO 5: return the type field
        return "";
    }
}
