package dev.javaquest.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Reads the library classpath that the app's runner uses, so the checks run code the same way the app does. */
final class RunnerClasspath {

    private RunnerClasspath() {
    }

    static List<Path> read(Path root) throws IOException {
        Path file = root.resolve("practice/target/runner-classpath.txt");
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(file + " is missing. Create it with: ./mvnw -q -f practice/pom.xml "
                    + "dependency:build-classpath -Dmdep.outputFile=practice/target/runner-classpath.txt");
        }
        return Arrays.stream(Files.readString(file).strip().split(java.io.File.pathSeparator))
                .filter(entry -> !entry.isBlank())
                .map(Path::of)
                .toList();
    }
}
