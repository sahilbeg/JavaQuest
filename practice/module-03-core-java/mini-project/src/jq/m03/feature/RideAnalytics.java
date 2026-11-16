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
        // TODO 3: add up the fare of every ride
        return 0;
    }

    public static Optional<String> busiestStation(List<Ride> rides) {
        // TODO 4: the station with the most minutes, or empty when there are no rides
        return Optional.empty();
    }

    public static TreeMap<String, Integer> minutesPerType(List<Ride> rides) {
        // TODO 5: total minutes per bike type, sorted by type
        return new TreeMap<>();
    }

    public static String report(ImportResult result) {
        // TODO 6: build the report from the KB page
        return "";
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
