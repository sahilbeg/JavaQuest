package jq.m12.redis;

/**
 * The options of SET that MiniRedis supports. Given to you, finished.
 *
 * <pre>
 * SET key value NX            -> SetOptions.nx()
 * SET key value PX 30000      -> SetOptions.px(30_000)
 * SET key value NX PX 30000   -> SetOptions.nxPx(30_000)
 * </pre>
 *
 * @param onlyIfAbsent NX: only set the key if it does not exist yet
 * @param pxMillis     PX: the key's time to live in milliseconds, or 0 for "no expiry"
 */
public record SetOptions(boolean onlyIfAbsent, long pxMillis) {

    public SetOptions {
        if (pxMillis < 0) {
            throw new IllegalArgumentException("ERR invalid expire time in 'set' command");
        }
    }

    public static SetOptions none() {
        return new SetOptions(false, 0);
    }

    public static SetOptions nx() {
        return new SetOptions(true, 0);
    }

    public static SetOptions px(long millis) {
        return new SetOptions(false, positive(millis));
    }

    public static SetOptions nxPx(long millis) {
        return new SetOptions(true, positive(millis));
    }

    private static long positive(long millis) {
        if (millis < 1) {
            throw new IllegalArgumentException("ERR invalid expire time in 'set' command");
        }
        return millis;
    }
}
