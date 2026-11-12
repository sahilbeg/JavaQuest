package jq.m05.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TelemetryIngester {

    public static Reading parse(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("expected 3 parts: " + line);
        }
        String bike = parts[0];
        try {
            return switch (parts[1]) {
                case "BATTERY" -> new Battery(bike, Integer.parseInt(parts[2]));
                case "GPS" -> {
                    String[] position = parts[2].split(",");
                    if (position.length != 2) {
                        throw new IllegalArgumentException("bad position: " + line);
                    }
                    yield new Gps(bike, Double.parseDouble(position[0]), Double.parseDouble(position[1]));
                }
                case "FAULT" -> new Fault(bike, parts[2]);
                default -> throw new IllegalArgumentException("unknown kind: " + parts[1]);
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad number: " + line, e);
        }
    }

    public static IngestReport ingest(List<String> lines, int workers) throws Exception {
        Map<String, Integer> lowest = new ConcurrentHashMap<>();
        Map<String, Integer> faults = new ConcurrentHashMap<>();
        AtomicInteger gpsFixes = new AtomicInteger();
        AtomicInteger badLines = new AtomicInteger();
        int chunkSize = Math.max(1, (lines.size() + workers - 1) / workers);
        try (ExecutorService pool = Executors.newFixedThreadPool(workers)) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int start = 0; start < lines.size(); start += chunkSize) {
                List<String> chunk = lines.subList(start, Math.min(lines.size(), start + chunkSize));
                tasks.add(pool.submit(() -> {
                    for (String line : chunk) {
                        try {
                            switch (parse(line)) {
                                case Battery(var bike, var percent) -> lowest.merge(bike, percent, Math::min);
                                case Fault(var bike, var code) -> faults.merge(bike, 1, Integer::sum);
                                case Gps g -> gpsFixes.incrementAndGet();
                            }
                        } catch (IllegalArgumentException e) {
                            badLines.incrementAndGet();
                        }
                    }
                }));
            }
            for (Future<?> task : tasks) {
                task.get();
            }
        }
        return new IngestReport(new TreeMap<>(lowest), new TreeMap<>(faults), gpsFixes.get(), badLines.get());
    }

    public static List<String> needsCharge(IngestReport report, int below) {
        List<String> bikes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : report.lowestBattery().entrySet()) {
            if (entry.getValue() < below) {
                bikes.add(entry.getKey());
            }
        }
        return bikes;
    }

    public static void main(String[] args) throws Exception {
        List<String> lines = List.of(
                "B-1042|BATTERY|80", "B-1042|BATTERY|15", "B-1042|GPS|51.51,-0.12",
                "B-2001|FAULT|E7", "B-2001|FAULT|E2", "B-3001|BATTERY|lots");
        IngestReport report = ingest(lines, 2);
        System.out.println(report);
        System.out.println("Needs charge: " + needsCharge(report, 20));
    }
}
