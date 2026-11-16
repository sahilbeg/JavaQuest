package jq.m08.incident;

import java.util.List;

/** Which bikes of a group unlock started a rental, and which did not. */
public record GroupResult(List<String> unlocked, List<String> failed) {
}
