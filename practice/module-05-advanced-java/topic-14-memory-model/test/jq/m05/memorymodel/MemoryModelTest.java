package jq.m05.memorymodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class MemoryModelTest {

    private static void runOnThreads(int threads, Runnable work) throws InterruptedException {
        Thread[] all = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            all[i] = new Thread(work);
            all[i].start();
        }
        for (Thread t : all) {
            t.join();
        }
    }

    private static List<Field> instanceFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) {
                fields.add(f);
            }
        }
        return fields;
    }

    // ---------- DockSensor ----------

    @Test
    @DisplayName("A new sensor is running, and stop() switches it off")
    void sensorStopFlag() {
        DockSensor sensor = new DockSensor();
        assertTrue(sensor.isRunning(), "A new DockSensor should report isRunning() == true");
        sensor.stop();
        assertFalse(sensor.isRunning(), "After stop(), isRunning() should return false. Set running = false in stop()");
    }

    @Test
    @DisplayName("stop() from another thread makes a running sensor finish within 2 seconds")
    void sensorStopsFromAnotherThread() {
        DockSensor sensor = new DockSensor();
        Thread worker = new Thread(sensor, "harbor-sensor");
        worker.setDaemon(true);
        worker.start();
        sensor.stop();
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> worker.join(),
                "The sensor thread kept looping after stop(). stop() must set running = false, and running must be volatile so the sensor thread sees it");
    }

    @Test
    @DisplayName("The running flag is declared volatile")
    void runningIsVolatile() {
        Field running;
        try {
            running = DockSensor.class.getDeclaredField("running");
        } catch (NoSuchFieldException e) {
            fail("DockSensor should keep its stop flag in a field named running");
            return;
        }
        assertTrue(Modifier.isVolatile(running.getModifiers()),
                "running is read by the sensor thread and written by another thread. Declare it: private volatile boolean running = true;");
    }

    // ---------- StationSnapshot ----------

    @Test
    @DisplayName("StationSnapshot is a final class whose fields are all private and final")
    void snapshotIsImmutable() {
        assertTrue(Modifier.isFinal(StationSnapshot.class.getModifiers()),
                "StationSnapshot should be declared final, so no subclass can add fields that change");
        List<Field> fields = instanceFields(StationSnapshot.class);
        assertFalse(fields.isEmpty(), "StationSnapshot should keep station, freeDocks and bikeIds in fields");
        for (Field f : fields) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), "The field " + f.getName() + " should be private");
            assertTrue(Modifier.isFinal(f.getModifiers()),
                    "The field " + f.getName() + " should be final. Final fields are what make a published snapshot safe to share");
        }
    }

    @Test
    @DisplayName("Changing the caller's list later does not change the snapshot")
    void snapshotCopiesTheList() {
        List<String> bikes = new ArrayList<>(List.of("B-1042", "B-2210"));
        StationSnapshot snap = new StationSnapshot("Harbor", 10, bikes);
        bikes.add("B-3001");
        bikes.set(0, "B-9999");
        assertEquals(List.of("B-1042", "B-2210"), snap.bikeIds(),
                "The snapshot should keep its own copy of the bike list. In the constructor use List.copyOf(bikeIds)");
        assertEquals("Harbor", snap.station(), "station() should return the station given to the constructor");
        assertEquals(10, snap.freeDocks(), "freeDocks() should return the number given to the constructor");
    }

    @Test
    @DisplayName("Nobody can add a bike to the list that bikeIds() returns")
    void snapshotListIsReadOnly() {
        StationSnapshot snap = new StationSnapshot("Uptown", 3, new ArrayList<>(List.of("B-1042")));
        assertThrows(UnsupportedOperationException.class, () -> snap.bikeIds().add("B-7777"),
                "bikeIds() should return a list that cannot be changed. List.copyOf gives you one");
        assertEquals(List.of("B-1042"), snap.bikeIds(), "The snapshot's bikes should still be just B-1042");
    }

    @Test
    @DisplayName("withFreeDocks returns a new snapshot and leaves the old one unchanged")
    void withFreeDocksMakesACopy() {
        StationSnapshot before = new StationSnapshot("Old Mill", 4, List.of("B-1042"));
        StationSnapshot after = before.withFreeDocks(5);
        assertNotSame(before, after, "withFreeDocks should return a NEW StationSnapshot, not this");
        assertEquals(4, before.freeDocks(), "The old snapshot should still say 4 free docks. Immutable objects never change");
        assertEquals(5, after.freeDocks(), "The new snapshot should say 5 free docks");
        assertEquals("Old Mill", after.station(), "The new snapshot should keep the station name");
        assertEquals(List.of("B-1042"), after.bikeIds(), "The new snapshot should keep the same bikes");
    }

    // ---------- RideStats ----------

    @Test
    @DisplayName("The peak keeps the biggest number, even when a smaller one comes later")
    void peakNeverGoesDown() {
        RideStats stats = new RideStats();
        stats.rideStarted(40);
        stats.rideStarted(12);
        stats.rideStarted(25);
        assertEquals(3, stats.rides(), "3 calls to rideStarted should count 3 rides");
        assertEquals(40, stats.peakOnline(),
                "peakOnline() should stay at 40, the biggest number reported. Use peakOnline.accumulateAndGet(ridersOnline, Math::max)");
    }

    @Test
    @DisplayName("6 kiosk threads x 5,000 rides give exactly 30,000 rides and the right peak")
    void statsUnderLoad() throws InterruptedException {
        RideStats stats = new RideStats();
        Thread[] kiosks = new Thread[6];
        for (int k = 0; k < kiosks.length; k++) {
            int kiosk = k;
            kiosks[k] = new Thread(() -> {
                for (int i = 0; i < 5_000; i++) {
                    stats.rideStarted(i % 500 + kiosk);
                }
            });
            kiosks[k].start();
        }
        for (Thread t : kiosks) {
            t.join();
        }
        assertEquals(30_000, stats.rides(),
                "rides() should be 30000 after 6 threads each start 5,000 rides. Count with a LongAdder: rides.increment(), then rides.sum()");
        assertEquals(504, stats.peakOnline(),
                "The biggest number any kiosk reported was 504 (499 + 5). Keep the peak with accumulateAndGet(ridersOnline, Math::max)");
    }

    @Test
    @DisplayName("RideStats keeps its numbers in atomics, not in volatile fields")
    void statsUseAtomics() {
        boolean hasAdder = false;
        boolean hasAtomic = false;
        for (Field f : instanceFields(RideStats.class)) {
            Class<?> type = f.getType();
            assertFalse(type.isPrimitive(),
                    "The field " + f.getName() + " is a plain " + type + ". Updating it from many threads races, even when it is volatile. Use LongAdder and AtomicInteger");
            if (type == LongAdder.class || type == AtomicLong.class) {
                hasAdder = true;
            }
            if (type == AtomicInteger.class) {
                hasAtomic = true;
            }
        }
        assertTrue(hasAdder, "RideStats should count rides in a field of type LongAdder");
        assertTrue(hasAtomic, "RideStats should keep the peak in a field of type AtomicInteger");
    }

    // ---------- FareTable ----------

    @Test
    @DisplayName("8 threads calling FareTable.get() all receive the same table")
    void fareTableIsShared() throws InterruptedException {
        Set<FareTable> seen = ConcurrentHashMap.newKeySet();
        AtomicInteger wrongFares = new AtomicInteger();
        runOnThreads(8, () -> {
            for (int i = 0; i < 100; i++) {
                FareTable table = FareTable.get();
                seen.add(table);
                if (table.fareCents("EBIKE", 10) != 400) {
                    wrongFares.incrementAndGet();
                }
            }
        });
        assertEquals(1, seen.size(),
                "Every call to FareTable.get() should return the same object, but " + seen.size()
                        + " different tables came back. Return Holder.INSTANCE instead of new FareTable()");
        assertEquals(0, wrongFares.get(), "An EBIKE ride of 10 minutes should cost 100 + 30 x 10 = 400 cents");
    }

    @Test
    @DisplayName("The fare table is built only once, however often get() is called")
    void fareTableBuiltOnce() throws InterruptedException {
        runOnThreads(4, () -> {
            for (int i = 0; i < 50; i++) {
                FareTable.get();
            }
        });
        assertEquals(1, FareTable.timesCreated(),
                "The FareTable constructor should run exactly once, but it ran " + FareTable.timesCreated()
                        + " times. Build it in a holder class: static final FareTable INSTANCE = new FareTable();");
    }
}
