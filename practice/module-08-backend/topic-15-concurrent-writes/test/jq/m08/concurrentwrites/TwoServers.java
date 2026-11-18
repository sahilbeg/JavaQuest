package jq.m08.concurrentwrites;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One in-memory database and two connections to it, standing in for two Spoke servers.
 * No threads and no timing: the tests decide exactly which server does what, and when.
 */
final class TwoServers implements AutoCloseable {

    private static final AtomicInteger NEXT_NAME = new AtomicInteger();

    final Connection serverA;
    final Connection serverB;

    TwoServers(String... setupSql) throws SQLException {
        // LOCK_TIMEOUT=200: a server that waits for a row another server has locked gives up after 0.2 s.
        String url = "jdbc:h2:mem:spoke-writes-" + NEXT_NAME.incrementAndGet() + ";LOCK_TIMEOUT=200";
        serverA = DriverManager.getConnection(url);
        serverB = DriverManager.getConnection(url);
        for (String sql : setupSql) {
            run(serverA, sql);
        }
    }

    static void run(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    static String text(Connection connection, String sql) throws SQLException {
        StringBuilder out = new StringBuilder();
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                if (out.length() > 0) {
                    out.append(", ");
                }
                out.append(rows.getString(1));
            }
        }
        return out.toString();
    }

    @Override
    public void close() throws SQLException {
        serverB.close();
        serverA.close();
    }

    /** Work the other server does, which may fail. */
    interface Sneak {
        void run() throws SQLException;
    }

    /**
     * Wraps a connection so that, just before the FIRST write (INSERT, UPDATE, DELETE or MERGE) sent through it,
     * the sneak runs once. That is the other server acting between your read and your write.
     */
    static final class SneakBeforeFirstWrite {
        final Connection connection;
        boolean sneaked;

        SneakBeforeFirstWrite(Connection real, Sneak sneak) {
            connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                        Object result = call(method, real, args);
                        if (result instanceof Statement statement) {
                            String preparedSql = method.getName().startsWith("prepare") ? (String) args[0] : null;
                            return wrap(statement, preparedSql, sneak);
                        }
                        return result;
                    });
        }

        private Object wrap(Statement real, String preparedSql, Sneak sneak) {
            Class<?>[] types = allStatementInterfaces(real);
            return Proxy.newProxyInstance(Statement.class.getClassLoader(), types, (proxy, method, args) -> {
                if (method.getName().startsWith("execute")) {
                    String sql = args != null && args.length > 0 && args[0] instanceof String s ? s : preparedSql;
                    if (!sneaked && isWrite(sql)) {
                        sneaked = true;
                        sneak.run();
                    }
                }
                return call(method, real, args);
            });
        }

        private static Class<?>[] allStatementInterfaces(Statement real) {
            if (real instanceof java.sql.CallableStatement) {
                return new Class<?>[] {java.sql.CallableStatement.class};
            }
            if (real instanceof java.sql.PreparedStatement) {
                return new Class<?>[] {java.sql.PreparedStatement.class};
            }
            return new Class<?>[] {Statement.class};
        }

        private static boolean isWrite(String sql) {
            if (sql == null) {
                return false;
            }
            String start = sql.strip().toUpperCase(Locale.ROOT);
            return start.startsWith("INSERT") || start.startsWith("UPDATE") || start.startsWith("DELETE")
                    || start.startsWith("MERGE");
        }
    }

    private static Object call(Method method, Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
