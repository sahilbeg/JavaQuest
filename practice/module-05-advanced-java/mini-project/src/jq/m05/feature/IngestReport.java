package jq.m05.feature;

import java.util.Map;

public record IngestReport(Map<String, Integer> lowestBattery, Map<String, Integer> faults, int gpsFixes, int badLines) {
}
