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
        main.put(Attributes.Name.MAIN_CLASS, mainClass);
        main.put(Attributes.Name.IMPLEMENTATION_TITLE, "Spoke kiosk");
        main.put(Attributes.Name.IMPLEMENTATION_VERSION, version);

        try (OutputStream file = Files.newOutputStream(jarFile); JarOutputStream jar = new JarOutputStream(file)) {
            jar.putNextEntry(entry(JarFile.MANIFEST_NAME));
            manifest.write(jar);
            jar.closeEntry();

            for (Path classFile : filesUnder(classesDir)) {
                jar.putNextEntry(entry(entryName(classesDir, classFile)));
                Files.copy(classFile, jar);
                jar.closeEntry();
            }
        }
    }

    /** The name inside the jar: the path below classesDir, with / between folders on every operating system. */
    static String entryName(Path classesDir, Path file) {
        return classesDir.relativize(file).toString().replace('\\', '/');
    }

    private static List<Path> filesUnder(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths.filter(Files::isRegularFile)
                    .sorted((a, b) -> entryName(dir, a).compareTo(entryName(dir, b)))
                    .toList();
        }
    }

    private static ZipEntry entry(String name) {
        ZipEntry entry = new ZipEntry(name);
        entry.setTimeLocal(FIXED_TIME);
        return entry;
    }
}
