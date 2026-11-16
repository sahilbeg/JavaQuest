package jq.m08.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

/** DataSources for the tests: a fresh H2 database, a wrapper that counts connections, and one that is always down. */
final class TestDataSources {

    private static final AtomicInteger NEXT_NAME = new AtomicInteger();

    private TestDataSources() {}

    /** A brand-new in-memory database. DB_CLOSE_DELAY=-1 keeps it alive between connections until shutdown(). */
    static JdbcDataSource freshH2() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:spoke-jdbc-" + NEXT_NAME.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    /** Drops the in-memory database so the next test starts clean. */
    static void shutdown(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }

    /** A DataSource whose getConnection always fails, like a database that is switched off. */
    static DataSource broken() {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[] {DataSource.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getConnection")) {
                        throw new SQLException("Connection refused: the database is down");
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    /** Wraps a real DataSource and counts how many connections were opened and how many were closed again. */
    static final class Counting {
        final AtomicInteger opened = new AtomicInteger();
        final AtomicInteger closed = new AtomicInteger();
        final DataSource dataSource;

        Counting(DataSource real) {
            dataSource = (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                    new Class<?>[] {DataSource.class}, (proxy, method, args) -> {
                        Object result = call(method, real, args);
                        if (method.getName().equals("getConnection")) {
                            opened.incrementAndGet();
                            return counted((Connection) result);
                        }
                        return result;
                    });
        }

        private Connection counted(Connection real) {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("close") && !real.isClosed()) {
                            closed.incrementAndGet();
                        }
                        return call(method, real, args);
                    });
        }

        private static Object call(Method method, Object target, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
