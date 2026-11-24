package dev.cadence.course;

import dev.cadence.content.ContentCatalog;
import dev.cadence.content.CourseLibrary;
import dev.cadence.content.CourseLibrary.Course;
import dev.cadence.content.CourseLibrary.Status;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/**
 * The learner's active course. Its catalog decides the order tickets open in, and its XP and badges make the level
 * and ladder. A learner who hasn't picked yet gets the first ready course behind the scenes, while the page sends
 * them to the course picker.
 */
@Service
public class Courses {

    private final CourseLibrary library;
    private final JdbcClient jdbc;
    private final boolean authorMode;
    private volatile String active;

    public Courses(CourseLibrary library, JdbcClient jdbc, dev.cadence.CadenceProperties properties) {
        this.library = library;
        this.jdbc = jdbc;
        this.authorMode = properties.unlockAll();
    }

    public CourseLibrary library() {
        return library;
    }

    /** The course the learner picked, if they have picked one. */
    public Optional<String> chosen() {
        String id = jdbc.sql("SELECT active_course FROM player WHERE id = 1").query(String.class).optional().orElse(null);
        return Optional.ofNullable(id).filter(c -> library.course(c).isPresent());
    }

    /** The active course id: the chosen one, or the first ready course. */
    public String activeId() {
        String cached = active;
        if (cached == null) {
            cached = chosen().orElseGet(() -> library.courses().stream()
                    .filter(c -> c.status() == Status.READY).findFirst().orElseThrow().id());
            active = cached;
        }
        return cached;
    }

    public Course active() {
        return library.course(activeId()).orElseThrow();
    }

    public ContentCatalog catalog() {
        return library.catalog(activeId());
    }

    /** Whether this course can be started: ready ones always, ones being written only in author mode. */
    public boolean canStart(Course course) {
        return course.status() == Status.READY
                || (authorMode && course.status() == Status.BUILDING && !library.catalog(course.id()).topics().isEmpty());
    }

    public Course choose(String courseId) {
        Course course = library.course(courseId)
                .orElseThrow(() -> new IllegalArgumentException("There is no course called " + courseId + "."));
        if (!canStart(course)) {
            throw new IllegalArgumentException(course.title() + " isn't ready yet. It opens when every module is written.");
        }
        jdbc.sql("UPDATE player SET active_course = ? WHERE id = 1").param(courseId).update();
        active = courseId;
        return course;
    }
}
