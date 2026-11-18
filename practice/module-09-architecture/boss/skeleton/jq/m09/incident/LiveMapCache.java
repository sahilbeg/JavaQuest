package jq.m09.incident;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The cache in front of the station-status service. The live map calls get() for every
 * station it shows, every 5 seconds, for every rider who has the app open.
 *
 * This is the code that is in production right now. It has three bugs.
 */
public class LiveMapCache {

    /** How long a loaded status counts as fresh. */
    public static final Duration TTL = Duration.ofSeconds(5);

    /** How long after it expires an old status may still be shown, while the source is down. */
    public static final Duration MAX_STALE = Duration.ofSeconds(60);

    /** While the source keeps failing, ask it again at most once per this interval. */
    public static final Duration RETRY_INTERVAL = Duration.ofSeconds(1);

    /** One cached status and the moment it was loaded. */
    private record Entry(StationStatus status, Instant loadedAt) {
    }

    private final StationStatusSource source;
    private final Clock clock;

    // TODO 1 (bronze): the key is the whole request, and every request has a new requestId,
    // so no two requests ever share an entry. Key the cache by the station id instead.
    private final Map<MapRequest, Entry> entries = new ConcurrentHashMap<>();

    public LiveMapCache(StationStatusSource source, Clock clock) {
        this.source = source;
        this.clock = clock;
    }

    /** The numbers the live map shows for the request's station. */
    public StationStatus get(MapRequest request) {
        Instant now = clock.instant();
        Entry entry = entries.get(request);
        if (entry != null && isFresh(entry, now)) {
            return entry.status();
        }
        // TODO 3 (silver): when an entry is missing or expired, every waiting request gets here
        // and calls the source. Let one request per station load; the others wait for its result.
        // A failed load must not stay in the cache.
        // TODO 4 (gold): when load throws, show the last status marked stale, for up to MAX_STALE
        // after it expired. Ask the source at most once per RETRY_INTERVAL while it is failing.
        // With nothing to show, throw StatusUnavailableException.
        StationStatus loaded = source.load(request.stationId());
        entries.put(request, new Entry(loaded, now));
        return loaded;
    }

    private static boolean isFresh(Entry entry, Instant now) {
        // TODO 2 (bronze): an entry is fresh only while now is before loadedAt + TTL.
        return true;
    }
}
