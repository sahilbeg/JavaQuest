package jq.m06.creational;

public enum PriceTable {
    INSTANCE;

    public int centsPerMinute(String type) {
        return switch (type) {
            case "classic" -> 15;
            case "ebike" -> 30;
            case "cargo" -> 25;
            default -> throw new IllegalArgumentException("unknown bike type: " + type);
        };
    }
}
