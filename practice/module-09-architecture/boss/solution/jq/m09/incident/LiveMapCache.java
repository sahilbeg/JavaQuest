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
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<StationStatus>> loading = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastFailure = new ConcurrentHashMap<>();

    public LiveMapCache(StationStatusSource source, Clock clock) {
        this.source = source;
        this.clock = clock;
    }

    /** The numbers the live map shows for the request's station. */
    public StationStatus get(MapRequest request) {
        String stationId = request.stationId();
        Entry entry = entries.get(stationId);
        if (entry != null && isFresh(entry, clock.instant())) {
            return entry.status();
        }
        CompletableFuture<StationStatus> mine = new CompletableFuture<>();
        CompletableFuture<StationStatus> running = loading.putIfAbsent(stationId, mine);
        if (running != null) {
            return await(running);
        }
        try {
            mine.complete(refresh(stationId));
        } catch (RuntimeException e) {
            mine.completeExceptionally(e);
        } finally {
            loading.remove(stationId, mine);
        }
        return await(mine);
    }

    /** Runs in exactly one thread per station at a time. */
    private StationStatus refresh(String stationId) {
        Instant now = clock.instant();
        Entry entry = entries.get(stationId);
        if (entry != null && isFresh(entry, now)) {
            return entry.status();
        }
        Instant failedAt = lastFailure.get(stationId);
        if (failedAt != null && now.isBefore(failedAt.plus(RETRY_INTERVAL))) {
            return fallback(stationId, entry, now, null);
        }
        try {
            StationStatus loaded = source.load(stationId);
            entries.put(stationId, new Entry(loaded, now));
            lastFailure.remove(stationId);
            return loaded;
        } catch (RuntimeException e) {
            lastFailure.put(stationId, now);
            return fallback(stationId, entry, now, e);
        }
    }

    private StationStatus fallback(String stationId, Entry entry, Instant now, RuntimeException cause) {
        if (entry != null && now.isBefore(entry.loadedAt().plus(TTL).plus(MAX_STALE))) {
            return entry.status().asStale();
        }
        throw new StatusUnavailableException(stationId, cause);
    }

    private static boolean isFresh(Entry entry, Instant now) {
        return now.isBefore(entry.loadedAt().plus(TTL));
    }

    private static StationStatus await(CompletableFuture<StationStatus> flight) {
        try {
            return flight.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}
