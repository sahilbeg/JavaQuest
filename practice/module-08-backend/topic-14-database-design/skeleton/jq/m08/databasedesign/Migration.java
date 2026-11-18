package jq.m08.databasedesign;

/** One versioned change to the schema, like Flyway's V2__rentals.sql: a version number, a short description and the SQL to run. */
public record Migration(int version, String description, String sql) {}
