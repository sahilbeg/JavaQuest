package jq.m03.feature;

import java.util.List;

public record ImportResult(List<Ride> rides, List<String> errors) {
}
