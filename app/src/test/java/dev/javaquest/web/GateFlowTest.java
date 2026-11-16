package dev.javaquest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.QuizSession;
import dev.javaquest.progress.ProgressService;
import java.io.IOException;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Tickets open one at a time: the next one opens when the one before it is mastered. */
@SpringBootTest
@AutoConfigureMockMvc
class GateFlowTest {

    private static final String FIRST = "module-00-foundations/topic-01-how-computers-work";
    private static final String SECOND = "module-00-foundations/topic-02-what-is-a-program";
    private static final String THIRD = "module-00-foundations/topic-03-terminal-basics";

    @Autowired
    MockMvc mvc;

    @Autowired
    ProgressService progress;

    @Autowired
    ContentCatalog catalog;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        dev.javaquest.TestRepository.register(registry, "gate-flow", false);
    }

    @Test
    void ticketsOpenOneAtATime() throws Exception {
        mvc.perform(get("/api/catalog"))
                .andExpect(jsonPath("$.authorMode").value(false))
                .andExpect(jsonPath("$.frontierId").value(FIRST))
                .andExpect(jsonPath("$.topics[0].locked").value(false))
                .andExpect(jsonPath("$.topics[1].locked").value(true));

        mvc.perform(get("/api/topics/" + FIRST)).andExpect(status().isOk());
        mvc.perform(get("/api/topics/" + SECOND))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", Matchers.containsString("SPK-0001")));
        mvc.perform(get("/api/topics/" + SECOND + "/quiz")).andExpect(status().isForbidden());
        mvc.perform(post("/api/topics/" + SECOND + "/read").header(LocalOnlyFilter.WRITE_HEADER, "1"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/topics/" + SECOND + "/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true));

        var first = catalog.topic(FIRST).orElseThrow();
        progress.recordQuiz(first, 1, new QuizSession.Result(100, true, List.of(), List.of()));

        mvc.perform(get("/api/catalog"))
                .andExpect(jsonPath("$.frontierId").value(SECOND))
                .andExpect(jsonPath("$.topics[0].locked").value(false))
                .andExpect(jsonPath("$.topics[1].locked").value(false))
                .andExpect(jsonPath("$.topics[2].locked").value(true));
        mvc.perform(get("/api/topics/" + SECOND)).andExpect(status().isOk());
        mvc.perform(get("/api/topics/" + THIRD)).andExpect(status().isForbidden());
        mvc.perform(get("/api/topics/" + FIRST)).andExpect(status().isOk());
    }
}
