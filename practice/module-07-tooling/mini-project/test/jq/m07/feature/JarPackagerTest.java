package jq.m07.feature;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JarPackagerTest {

    @TempDir
    Path temp;

    /** A small fake classes folder, written in the order given. */
    private Path classes(String folder, String... names) throws IOException {
        Path dir = Files.createDirectories(temp.resolve(folder));
        for (String name : names) {
            Path file = dir.resolve(name);
            Files.createDirectories(file.getParent());
            Files.writeString(file, "bytes of " + name);
        }
        return dir;
    }

    @Test
    @DisplayName("The manifest names the main class, the title and the version")
    void manifest() throws IOException {
        Path jar = temp.resolve("kiosk.jar");
        JarPackager.pack(classes("classes", "jq/m07/feature/Kiosk.class"), jar, "jq.m07.feature.Kiosk", "1.10.0");
        try (JarFile file = new JarFile(jar.toFile())) {
            Manifest manifest = file.getManifest();
            assertNotNull(manifest, "the jar needs a META-INF/MANIFEST.MF entry");
            Attributes main = manifest.getMainAttributes();
            assertEquals("1.0", main.getValue(Attributes.Name.MANIFEST_VERSION), "Manifest-Version should be 1.0");
            assertEquals("jq.m07.feature.Kiosk", main.getValue(Attributes.Name.MAIN_CLASS), "Main-Class tells java -jar where main is");
            assertEquals("Spoke kiosk", main.getValue(Attributes.Name.IMPLEMENTATION_TITLE), "Implementation-Title should be Spoke kiosk");
            assertEquals("1.10.0", main.getValue(Attributes.Name.IMPLEMENTATION_VERSION), "Implementation-Version should be the version passed in");
        }
    }

    @Test
    @DisplayName("The manifest comes first, then every file sorted by name, with / between folders")
    void entries() throws IOException {
        Path dir = classes("classes", "kiosk-banner.txt", "jq/m07/feature/Kiosk.class", "jq/m07/feature/BuildInfo.class");
        Path jar = temp.resolve("kiosk.jar");
        JarPackager.pack(dir, jar, "jq.m07.feature.Kiosk", "1.10.0");
        try (JarFile file = new JarFile(jar.toFile())) {
            List<String> names = Collections.list(file.entries()).stream().map(ZipEntry::getName).toList();
            assertEquals(List.of("META-INF/MANIFEST.MF", "jq/m07/feature/BuildInfo.class", "jq/m07/feature/Kiosk.class", "kiosk-banner.txt"),
                    names, "the manifest first, then only files (no folders), named from the classes folder and sorted");
            ZipEntry kiosk = file.getEntry("jq/m07/feature/Kiosk.class");
            assertEquals("bytes of jq/m07/feature/Kiosk.class", new String(file.getInputStream(kiosk).readAllBytes()),
                    "each entry holds the file's bytes");
        }
    }

    @Test
    @DisplayName("The same classes always give the same jar, byte for byte")
    void reproducible() throws IOException {
        Path first = classes("first", "b/Two.class", "a/One.class");
        Path second = classes("second", "a/One.class", "b/Two.class");
        Files.setLastModifiedTime(second.resolve("a/One.class"), FileTime.fromMillis(0));
        JarPackager.pack(first, temp.resolve("first.jar"), "a.One", "1.10.0");
        JarPackager.pack(second, temp.resolve("second.jar"), "a.One", "1.10.0");

        try (JarFile file = new JarFile(temp.resolve("first.jar").toFile())) {
            List<JarEntry> entries = Collections.list(file.entries());
            assertEquals(3, entries.size(), "the jar should hold the manifest and the two class files");
            for (JarEntry entry : entries) {
                assertEquals(JarPackager.FIXED_TIME, entry.getTimeLocal(), entry.getName() + " should have the time FIXED_TIME, not the time of the build");
            }
        }
        assertArrayEquals(Files.readAllBytes(temp.resolve("first.jar")), Files.readAllBytes(temp.resolve("second.jar")),
                "the files were written in a different order and at different times, but the two jars should be identical");
    }
}
