package jq.m08.boot;

/** What GET /health sends back, for example {"status":"UP","stations":5}. */
public record Health(String status, int stations) {
}
