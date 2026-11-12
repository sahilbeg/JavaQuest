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
        // TODO 1: split on |, then build a Battery, Gps or Fault; throw IllegalArgumentException otherwise
        throw new IllegalArgumentException("not written yet: " + line);
    }

    public static IngestReport ingest(List<String> lines, int workers) throws Exception {
        // TODO 2: split lines into chunks, run them on a fixed pool, and combine the results safely
        return new IngestReport(new TreeMap<>(), new TreeMap<>(), 0, 0);
    }

    public static List<String> needsCharge(IngestReport report, int below) {
        // TODO 3: bike ids whose lowest battery is under `below`, sorted
        return new ArrayList<>();
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
