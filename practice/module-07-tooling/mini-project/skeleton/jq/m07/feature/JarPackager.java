package jq.m07.feature;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/** Packs a folder of compiled classes into a runnable jar, the way the jar tool and Maven's jar plugin do. */
public final class JarPackager {

    /**
     * Every entry gets this date and time, so the same classes always give a byte-for-byte identical jar.
     * It is a local date and time with no time zone, so a laptop and a CI machine in UTC agree.
     */
    public static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    private JarPackager() {
    }

    public static void pack(Path classesDir, Path jarFile, String mainClass, String version) throws IOException {
        Manifest manifest = new Manifest();
        Attributes main = manifest.getMainAttributes();
        main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        // TODO 5: put MAIN_CLASS, IMPLEMENTATION_TITLE ("Spoke kiosk") and IMPLEMENTATION_VERSION.

        try (OutputStream file = Files.newOutputStream(jarFile); JarOutputStream jar = new JarOutputStream(file)) {
            // TODO 6: write the manifest as the first entry, named JarFile.MANIFEST_NAME. Use entry(name).
            // TODO 7: add every file from filesUnder(classesDir), named entryName(classesDir, file), with its bytes.
        }
    }

    /** The name inside the jar: the path below classesDir, with / between folders on every operating system. */
    static String entryName(Path classesDir, Path file) {
        // TODO 8: the path of file below classesDir, with '/' between folders even on Windows.
        return "";
    }

    private static List<Path> filesUnder(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            // TODO 9: sort the files by entry name, so the order never depends on the file system.
            return paths.filter(Files::isRegularFile).toList();
        }
    }

    private static ZipEntry entry(String name) {
        ZipEntry entry = new ZipEntry(name);
        // TODO 10: give every entry the same date and time, FIXED_TIME, with entry.setTimeLocal.
        //          Without it, each entry gets the time you happen to build.
        return entry;
    }
}
