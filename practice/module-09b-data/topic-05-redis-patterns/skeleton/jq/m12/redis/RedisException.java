package jq.m12.redis;

/**
 * An error reply, like the ones Redis sends back. The message starts with the same code Redis uses:
 * {@code ERR} for a bad argument, {@code WRONGTYPE} for a command used on the wrong kind of value.
 * Given to you, finished.
 */
public class RedisException extends RuntimeException {

    public static final String NOT_AN_INTEGER = "ERR value is not an integer or out of range";
    public static final String OVERFLOW = "ERR increment or decrement would overflow";
    public static final String WRONG_TYPE = "WRONGTYPE Operation against a key holding the wrong kind of value";

    public RedisException(String message) {
        super(message);
    }
}
