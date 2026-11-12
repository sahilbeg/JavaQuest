package dev.javaquest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Points a Spring test at a throwaway copy of content/ and practice/. Tests that save or reset practice
 * files must never touch the real repository: a running JavaQuest would record those saves as the learner's.
 */
public final class TestRepository {

    private TestRepository() {}

    public static Path register(DynamicPropertyRegistry registry, String databaseName) throws IOException {
        Path repo = Path.of("..").toAbsolutePath().normalize();
        Path copy = Files.createTempDirectory("javaquest-repo");
        copyTree(repo.resolve("content"), copy.resolve("content"));
        copyTree(repo.resolve("practice"), copy.resolve("practice"));
        Path classpathFile = Files.createTempFile("runner-classpath", ".txt");
        Files.writeString(classpathFile, System.getProperty("junit.console.jar"));
        registry.add("javaquest.root", copy::toString);
        registry.add("javaquest.runner.classpath-file", classpathFile::toString);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1");
        return copy;
    }

    private static void copyTree(Path from, Path to) throws IOException {
        try (Stream<Path> paths = Files.walk(from)) {
            paths.filter(p -> !p.toString().contains("/target/")).forEach(source -> {
                try {
                    Path target = to.resolve(from.relativize(source).toString());
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
