package jq.m03.exceptions;

import java.util.List;

public record ImportResult(List<Ride> rides, int badLines) {
}
