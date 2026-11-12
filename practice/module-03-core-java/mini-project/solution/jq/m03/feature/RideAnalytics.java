package jq.m03.feature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RideAnalytics {

    public static double revenue(List<Ride> rides) {
        return rides.stream().mapToDouble(Ride::fare).sum();
    }

    public static Optional<String> busiestStation(List<Ride> rides) {
        Map<String, Integer> minutes = rides.stream()
                .collect(Collectors.groupingBy(Ride::station, TreeMap::new, Collectors.summingInt(Ride::minutes)));
        return minutes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public static TreeMap<String, Integer> minutesPerType(List<Ride> rides) {
        return rides.stream()
                .collect(Collectors.groupingBy(Ride::type, TreeMap::new, Collectors.summingInt(Ride::minutes)));
    }

    public static String report(ImportResult result) {
        List<Ride> rides = result.rides();
        StringBuilder text = new StringBuilder();
        text.append(String.format("Rides: %d (%d bad lines)", rides.size(), result.errors().size())).append("\n");
        text.append(String.format("Revenue: $%.2f", revenue(rides))).append("\n");
        text.append("Busiest station: ").append(busiestStation(rides).orElse("none"));
        minutesPerType(rides).forEach((type, minutes) -> text.append("\n").append(type).append(": ").append(minutes).append(" min"));
        result.errors().forEach(error -> text.append("\n").append(error));
        return text.toString();
    }

    public static void main(String[] args) throws IOException {
        Path sample = Files.createTempFile("harbor", ".csv");
        Files.writeString(sample, """
                bikeId,station,type,minutes
                B-1042,Harbor,ebike,40
                B-2001,Riverside,classic,30
                B-3001,Harbor,cargo,twenty
                B-4001,Old Mill,tandem,10

                B-5001,Harbor,ebike,25
                """);
        System.out.println(report(RideLogReader.read(sample)));
    }
}
