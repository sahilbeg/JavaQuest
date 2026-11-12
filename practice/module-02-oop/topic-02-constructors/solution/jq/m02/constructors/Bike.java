package jq.m02.constructors;

public class Bike {
    public String id;
    public String type;

    public Bike(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public Bike(String id) {
        this(id, "classic");
    }
}
