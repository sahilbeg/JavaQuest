package jq.m12.incident;

import java.time.LocalDateTime;

/** One ride in the city council's CSV export. startedAt is Riverton local time. */
public record ExportRow(long id, String riderId, String stationId, LocalDateTime startedAt, long fareCents) {}
