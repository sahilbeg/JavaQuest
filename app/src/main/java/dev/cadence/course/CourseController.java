package dev.cadence.course;

import dev.cadence.content.CourseLibrary.Course;
import dev.cadence.content.CourseLibrary.Objective;
import dev.cadence.content.CourseLibrary.PlannedModule;
import dev.cadence.content.Topic;
import dev.cadence.progress.ProgressService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The course picker: goals, the courses for each, each course's path, and the learner's choice. */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    /** A module on a course's path. {@code tickets} is 0 while the module is still being written. */
    public record PathModule(String id, String title, String project, int tickets, int mastered) {}

    public record CourseView(String id, String objective, String title, String summary, List<String> stack, String status,
                             boolean canStart, int tickets, int mastered, int minutes, List<PathModule> path, List<String> ladder) {}

    public record Picker(List<Objective> objectives, List<CourseView> courses, String active, boolean chosen) {}

    public record Choice(String courseId) {}

    private final Courses courses;
    private final ProgressService progress;

    public CourseController(Courses courses, ProgressService progress) {
        this.courses = courses;
        this.progress = progress;
    }

    @GetMapping
    public Picker picker() {
        var library = courses.library();
        var views = library.courses().stream().map(this::view).toList();
        return new Picker(library.objectives(), views, courses.activeId(), courses.chosen().isPresent());
    }

    @PutMapping("/active")
    public CourseView choose(@RequestBody Choice choice) {
        return view(courses.choose(choice.courseId()));
    }

    private CourseView view(Course course) {
        var catalog = courses.library().catalog(course.id());
        var path = courses.library().path(course.id()).stream().map(planned -> pathModule(planned, catalog)).toList();
        int tickets = catalog.topics().size();
        int mastered = (int) catalog.topics().stream().filter(t -> progress.progress(t).mastered()).count();
        int minutes = catalog.topics().stream().mapToInt(t -> t.meta().minutes()).sum();
        return new CourseView(course.id(), course.objective(), course.title(), course.summary(), course.stack(),
                course.status().name().toLowerCase(), courses.canStart(course), tickets, mastered, minutes, path, course.ladder());
    }

    private PathModule pathModule(PlannedModule planned, dev.cadence.content.ContentCatalog catalog) {
        var module = catalog.modules().stream().filter(m -> m.id().equals(planned.id())).findFirst();
        List<Topic> topics = catalog.topics().stream().filter(t -> t.meta().moduleId().equals(planned.id())).toList();
        int mastered = (int) topics.stream().filter(t -> progress.progress(t).mastered()).count();
        return new PathModule(planned.id(), planned.title(), module.map(m -> m.project()).orElse(null), topics.size(), mastered);
    }
}
