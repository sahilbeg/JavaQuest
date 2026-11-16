package jq.m07.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Packs your compiled kiosk classes into a real jar, then starts it with java -jar in a new JVM. */
class RunnableJarTest {

    private record Result(int exitCode, String out, String err) {}

    @TempDir
    Path temp;

    private Path jar;

    @BeforeEach
    void packTheKiosk() throws Exception {
        Path compiled = Path.of(Kiosk.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path packageDir = compiled.resolve("jq/m07/feature");
        Path classes = temp.resolve("classes");
        try (Stream<Path> files = Files.list(packageDir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".class")).toList()) {
                Path target = classes.resolve(compiled.relativize(file).toString());
                Files.createDirectories(target.getParent());
                Files.copy(file, target);
            }
        }
        jar = temp.resolve("kiosk.jar");
        JarPackager.pack(classes, jar, "jq.m07.feature.Kiosk", "1.10.0");
    }

    private Result javaJar(String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-jar", jar.toString()));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command)
                .redirectOutput(temp.resolve("out.txt").toFile())
                .redirectError(temp.resolve("err.txt").toFile())
                .start();
        assertTrue(process.waitFor(20, TimeUnit.SECONDS), "java -jar kiosk.jar should finish within 20 seconds");
        return new Result(process.exitValue(),
                Files.readString(temp.resolve("out.txt"), StandardCharsets.UTF_8).strip(),
                Files.readString(temp.resolve("err.txt"), StandardCharsets.UTF_8).strip());
    }

    @Test
    @DisplayName("java -jar kiosk.jar --version prints the version from the manifest")
    void versionFromManifest() throws Exception {
        Result result = javaJar("--version");
        assertEquals("Spoke kiosk 1.10.0", result.out(),
                "inside the jar, BuildInfo.version() should read Implementation-Version from the manifest. Errors: " + result.err());
        assertEquals(0, result.exitCode(), "--version should exit with code 0");
    }

    @Test
    @DisplayName("java -jar kiosk.jar 12 prints the fare")
    void fareFromJar() throws Exception {
        Result result = javaJar("12");
        assertEquals("Fare for 12 min: $2.80", result.out(), "the jar should start Kiosk.main. Errors: " + result.err());
        assertEquals(0, result.exitCode(), "a good command exits with code 0");
    }

    @Test
    @DisplayName("java -jar kiosk.jar with no minutes exits with code 2")
    void exitCodeFromJar() throws Exception {
        Result result = javaJar();
        assertEquals(2, result.exitCode(), "main should pass run's exit code to System.exit. Errors: " + result.err());
        assertEquals(Kiosk.USAGE, result.err(), "the usage line goes to the error stream");
    }
}
