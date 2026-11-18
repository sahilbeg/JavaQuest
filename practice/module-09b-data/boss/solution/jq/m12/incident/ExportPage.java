package jq.m12.incident;

import java.util.List;

/** One page of the export, and the cursor to ask for the next page with (null when this was the last page). */
public record ExportPage(List<ExportRow> rows, String nextCursor) {}
