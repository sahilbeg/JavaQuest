package jq.m08.concurrentwrites;

import java.sql.Connection;
import java.sql.SQLException;

/** A piece of database work that can run again from the start. */
@FunctionalInterface
public interface SqlWork<T> {
    T run(Connection connection) throws SQLException;
}
