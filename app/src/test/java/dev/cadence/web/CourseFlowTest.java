package dev.cadence.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CourseFlowTest {

    @Autowired
    MockMvc mvc;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        dev.cadence.TestRepository.register(registry, "course-flow", false);
    }

    @Test
    @DisplayName("A new learner picks a course, and a course that isn't written yet can't be started")
    void aNewLearnerPicksAReadyCourse() throws Exception {
        mvc.perform(get("/api/catalog"))
                .andExpect(jsonPath("$.course.chosen").value(false));
        mvc.perform(get("/api/courses"))
                .andExpect(jsonPath("$.chosen").value(false))
                .andExpect(jsonPath("$.objectives.length()").value(4))
                .andExpect(jsonPath("$.courses[?(@.id == 'frontend-react')].status").value(Matchers.contains("ready")))
                .andExpect(jsonPath("$.courses[?(@.id == 'backend-node')].status").value(Matchers.contains("soon")))
                .andExpect(jsonPath("$.courses[?(@.id == 'frontend-react')].path.length()").value(Matchers.contains(11)))
                .andExpect(jsonPath("$.courses[?(@.id == 'java-swe')].canStart").value(Matchers.contains(true)));

        mvc.perform(put("/api/courses/active").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"courseId\": \"backend-node\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/courses/active").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"courseId\": \"java-swe\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/catalog"))
                .andExpect(jsonPath("$.course.chosen").value(true))
                .andExpect(jsonPath("$.course.title").value("Software engineering in Java"));
        mvc.perform(get("/api/career"))
                .andExpect(jsonPath("$.level.courseId").value("java-swe"))
                .andExpect(jsonPath("$.level.ladder[0].title").value("Intern"));
    }
}
