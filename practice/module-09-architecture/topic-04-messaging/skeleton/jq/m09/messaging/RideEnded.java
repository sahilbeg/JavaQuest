package jq.m09.messaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The event "a ride has ended", named in the past tense because it already happened. */
public record RideEnded(String eventId, String rentalId, String riderId, String bikeId, BikeType type, int minutes,
                        long fareCents) {

    private static final Pattern FIELD = Pattern.compile("\"(\\w+)\":(\"([^\"]*)\"|(-?\\d+))");

    /** This event as one line of JSON, such as {"eventId":"evt-1001","rentalId":"rental-17",...}. */
    public String toJson() {
        return "{\"eventId\":\"" + eventId + "\",\"rentalId\":\"" + rentalId + "\",\"riderId\":\"" + riderId
                + "\",\"bikeId\":\"" + bikeId + "\",\"type\":\"" + type + "\",\"minutes\":" + minutes
                + ",\"fareCents\":" + fareCents + "}";
    }

    /** Reads the JSON that toJson() writes. Fields it does not know are skipped, so newer events still parse. */
    public static RideEnded fromJson(String json) {
        String eventId = null;
        String rentalId = null;
        String riderId = null;
        String bikeId = null;
        BikeType type = null;
        int minutes = 0;
        long fareCents = 0;
        Matcher field = FIELD.matcher(json);
        while (field.find()) {
            String text = field.group(3) != null ? field.group(3) : field.group(4);
            switch (field.group(1)) {
                case "eventId" -> eventId = text;
                case "rentalId" -> rentalId = text;
                case "riderId" -> riderId = text;
                case "bikeId" -> bikeId = text;
                case "type" -> type = BikeType.valueOf(text);
                case "minutes" -> minutes = Integer.parseInt(text);
                case "fareCents" -> fareCents = Long.parseLong(text);
                default -> { }
            }
        }
        return new RideEnded(eventId, rentalId, riderId, bikeId, type, minutes, fareCents);
    }
}
