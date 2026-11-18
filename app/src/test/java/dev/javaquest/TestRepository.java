package dev.javaquest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Points a Spring test at a throwaway copy of content/ and practice/. Tests that save or reset practice
 * files must never touch the real repository: a running JavaQuest would record those saves as the learner's.
 */
public final class TestRepository {

    private TestRepository() {}

    public static Path register(DynamicPropertyRegistry registry, String databaseName) throws IOException {
        return register(registry, databaseName, true);
    }

    /** Most tests open every ticket (author mode); gating tests pass unlockAll = false. */
    public static Path register(DynamicPropertyRegistry registry, String databaseName, boolean unlockAll) throws IOException {
        Path repo = Path.of("..").toAbsolutePath().normalize();
        Path copy = Files.createTempDirectory("javaquest-repo");
        copyTree(repo.resolve("content"), copy.resolve("content"));
        copyTree(repo.resolve("practice"), copy.resolve("practice"));
        resetSourcesToSkeletons(copy.resolve("practice"));
        Path classpathFile = Files.createTempFile("runner-classpath", ".txt");
        Files.writeString(classpathFile, System.getProperty("junit.console.jar"));
        registry.add("javaquest.root", copy::toString);
        registry.add("javaquest.unlock-all", () -> String.valueOf(unlockAll));
        registry.add("javaquest.runner.classpath-file", classpathFile::toString);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1");
        return copy;
    }

    /**
     * Each ticket's src/ holds the learner's own code, which may already solve it. Tests expect the untouched
     * starting code, so the copy's src/ is replaced by its skeleton/ wherever a unit has one.
     */
    private static void resetSourcesToSkeletons(Path practice) throws IOException {
        try (Stream<Path> modules = Files.list(practice)) {
            for (Path module : modules.filter(Files::isDirectory).toList()) {
                try (Stream<Path> units = Files.list(module)) {
                    for (Path unit : units.filter(u -> Files.isDirectory(u.resolve("skeleton"))).toList()) {
                        Path src = unit.resolve("src");
                        deleteTree(src);
                        copyTree(unit.resolve("skeleton"), src);
                    }
                }
            }
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
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
