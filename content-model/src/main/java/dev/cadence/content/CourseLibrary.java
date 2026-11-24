package dev.cadence.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Every course a learner can pick, read from courses.yaml at the repository root.
 *
 * <p>Content lives in pools (content folders): the Java course in content/ and practice/, the web courses in
 * tracks/web/. A course is a list of modules from those pools, so courses can share modules. Topic ids are unique
 * across pools, so progress never mixes, and the whole library can still look a topic up by id.
 */
public final class CourseLibrary {

    private static final YAMLMapper YAML = YAMLMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public enum Status {
        /** Every module is written: the learner can start it. */
        @com.fasterxml.jackson.annotation.JsonProperty("ready") READY,
        /** Being written: shown with its path, opens only in author mode. */
        @com.fasterxml.jackson.annotation.JsonProperty("building") BUILDING,
        /** Planned: shown so learners see what's coming. */
        @com.fasterxml.jackson.annotation.JsonProperty("soon") SOON
    }

    public record Pool(String id, String content, String practice) {
        public Pool {
            require(id != null && id.matches("[a-z]+"), "pool id must be lowercase letters, got: " + id);
            require(content != null && practice != null, "pool " + id + " needs content and practice folders");
        }
    }

    public record Objective(String id, String title, String summary) {
        public Objective {
            require(id != null && id.matches("[a-z-]+"), "objective id must be lowercase words joined by -, got: " + id);
            require(title != null && !title.isBlank(), "objective " + id + " needs a title");
        }
    }

    /** A planned module: its id and title, so the path shows before the module is written. */
    public record PlannedModule(String id, String title) {
        public PlannedModule {
            require(id != null && id.matches(ModuleInfo.ID_PATTERN), "planned module id must look like web-03-name, got: " + id);
            require(title != null && !title.isBlank(), "planned module " + id + " needs a title");
        }
    }

    /**
     * @param pool    with no {@code modules}, the course is every module of this pool, in folder order
     * @param modules the course's modules in order, written or not
     * @param ladder  the job titles, one per 5 levels, from the first to the top
     */
    public record Course(String id, String objective, String title, String summary, List<String> stack, Status status,
                         String pool, List<PlannedModule> modules, List<String> ladder) {
        public Course {
            require(id != null && id.matches("[a-z0-9-]+"), "course id must be lowercase words joined by -, got: " + id);
            require(objective != null, "course " + id + " needs an objective");
            require(title != null && !title.isBlank(), "course " + id + " needs a title");
            require(status != null, "course " + id + " needs a status: ready, building or soon");
            require(ladder != null && ladder.size() >= 2, "course " + id + " needs a ladder of job titles");
            stack = stack == null ? List.of() : List.copyOf(stack);
            modules = modules == null ? List.of() : List.copyOf(modules);
            ladder = List.copyOf(ladder);
        }
    }

    private record File(List<Pool> pools, List<Objective> objectives, List<Course> courses) {}

    /** Without a courses.yaml (an older checkout, a test folder), the one course is everything in content/. */
    private static final File JAVA_ONLY = new File(
            List.of(new Pool("java", "content", "practice")),
            List.of(new Objective("software-engineering", "Software engineer", null)),
            List.of(new Course("java-swe", "software-engineering", "Software engineering in Java", null, List.of("Java"),
                    Status.READY, "java", null, List.of("Intern", "Junior Developer", "Developer", "Software Engineer",
                    "Senior Engineer", "Staff Engineer", "Architect", "Principal Engineer"))));

    private final List<Pool> pools;
    private final List<Objective> objectives;
    private final List<Course> courses;
    private final ContentCatalog library;
    private final Map<String, ContentCatalog> catalogs;

    private CourseLibrary(List<Pool> pools, List<Objective> objectives, List<Course> courses, ContentCatalog library,
                          Map<String, ContentCatalog> catalogs) {
        this.pools = List.copyOf(pools);
        this.objectives = List.copyOf(objectives);
        this.courses = List.copyOf(courses);
        this.library = library;
        this.catalogs = Map.copyOf(catalogs);
    }

    /** Reads courses.yaml and every pool it names. A pool whose content folder doesn't exist yet is empty. */
    public static CourseLibrary load(Path root) throws IOException {
        Path file = root.resolve("courses.yaml");
        File spec;
        if (!Files.isRegularFile(file)) {
            spec = JAVA_ONLY;
        } else {
            try {
                spec = YAML.readValue(file.toFile(), File.class);
            } catch (JacksonException e) {
                Throwable cause = e.getCause() instanceof IllegalArgumentException invalid ? invalid : e;
                throw new InvalidContentException(file, cause.getMessage());
            }
        }
        var pools = new LinkedHashMap<String, ContentCatalog>();
        for (Pool pool : spec.pools()) {
            Path content = root.resolve(pool.content());
            pools.put(pool.id(), Files.isDirectory(content) ? ContentCatalog.load(content, root.resolve(pool.practice())) : ContentCatalog.empty());
        }
        ContentCatalog library = ContentCatalog.merge(new ArrayList<>(pools.values()));
        var objectiveIds = spec.objectives().stream().map(Objective::id).toList();
        var catalogs = new LinkedHashMap<String, ContentCatalog>();
        for (Course course : spec.courses()) {
            if (!objectiveIds.contains(course.objective())) {
                throw new InvalidContentException(file, "course " + course.id() + " has an unknown objective " + course.objective());
            }
            if (catalogs.containsKey(course.id())) {
                throw new InvalidContentException(file, "course id " + course.id() + " is used twice");
            }
            ContentCatalog catalog;
            if (course.pool() != null) {
                ContentCatalog pool = pools.get(course.pool());
                if (pool == null) {
                    throw new InvalidContentException(file, "course " + course.id() + " names an unknown pool " + course.pool());
                }
                catalog = pool;
            } else {
                catalog = library.subset(course.modules().stream().map(PlannedModule::id).toList());
            }
            if (course.status() == Status.READY && catalog.topics().isEmpty()) {
                throw new InvalidContentException(file, "course " + course.id() + " is ready but has no topics");
            }
            catalogs.put(course.id(), catalog);
        }
        return new CourseLibrary(spec.pools(), spec.objectives(), spec.courses(), library, catalogs);
    }

    /** The content pools, for the folders a learner's code lives in. */
    public List<Pool> pools() {
        return pools;
    }

    public List<Objective> objectives() {
        return objectives;
    }

    public List<Course> courses() {
        return courses;
    }

    public Optional<Course> course(String id) {
        return courses.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    /** Every topic of every pool, for looking a topic up by id. */
    public ContentCatalog library() {
        return library;
    }

    /** The course's written modules and topics, in course order. */
    public ContentCatalog catalog(String courseId) {
        ContentCatalog catalog = catalogs.get(courseId);
        if (catalog == null) {
            throw new IllegalArgumentException("No course " + courseId);
        }
        return catalog;
    }

    /** The course's full path: written modules with their real titles, then planned ones. */
    public List<PlannedModule> path(String courseId) {
        Course course = course(courseId).orElseThrow(() -> new IllegalArgumentException("No course " + courseId));
        if (course.pool() != null) {
            return catalog(courseId).modules().stream().map(m -> new PlannedModule(m.id(), m.title())).toList();
        }
        return course.modules().stream()
                .map(planned -> library.modules().stream().filter(m -> m.id().equals(planned.id())).findFirst()
                        .map(m -> new PlannedModule(m.id(), m.title())).orElse(planned))
                .toList();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
