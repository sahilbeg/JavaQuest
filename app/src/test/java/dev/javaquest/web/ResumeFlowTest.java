package dev.javaquest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.QuizSession;
import dev.javaquest.progress.ProgressService;
import java.io.IOException;
import java.util.List;
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
import org.springframework.test.web.servlet.ResultActions;

/** The last screen inside an unfinished ticket is kept with the progress, so Continue can open it again. */
@SpringBootTest
@AutoConfigureMockMvc
class ResumeFlowTest {

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
        dev.javaquest.TestRepository.register(registry, "resume-flow", false);
    }

    private ResultActions savePosition(String id, String json) throws Exception {
        return mvc.perform(put("/api/topics/" + id + "/position").header(LocalOnlyFilter.WRITE_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    @DisplayName("Continue opens the last card of the unfinished ticket, and moves on once it is finished")
    void continueFollowsTheLastScreen() throws Exception {
        mvc.perform(get("/api/resume")).andExpect(status().isOk()).andExpect(content().string(""));
        mvc.perform(get("/api/topics/" + FIRST + "/position")).andExpect(content().string(""));

        savePosition(FIRST, "{\"step\":4,\"furthest\":5,\"total\":14,\"label\":\"Memory and storage\"}")
                .andExpect(status().isOk());
        mvc.perform(get("/api/topics/" + FIRST + "/position"))
                .andExpect(jsonPath("$.step").value(4))
                .andExpect(jsonPath("$.furthest").value(5))
                .andExpect(jsonPath("$.label").value("Memory and storage"));
        mvc.perform(get("/api/resume"))
                .andExpect(jsonPath("$.topicId").value(FIRST))
                .andExpect(jsonPath("$.key").value("SPK-0001"))
                .andExpect(jsonPath("$.step").value(4))
                .andExpect(jsonPath("$.total").value(14));

        // A locked ticket cannot save a place.
        savePosition(THIRD, "{\"step\":1,\"furthest\":1,\"total\":10,\"label\":\"Hook\"}")
                .andExpect(status().isForbidden());

        // Finishing the ticket: Continue moves to the next unfinished screen, here none yet, then the new ticket's.
        progress.recordQuiz(catalog.topic(FIRST).orElseThrow(), 1, new QuizSession.Result(100, true, List.of(), List.of()));
        mvc.perform(get("/api/resume")).andExpect(content().string(""));
        savePosition(SECOND, "{\"step\":2,\"furthest\":2,\"total\":12,\"label\":\"Concept\"}").andExpect(status().isOk());
        mvc.perform(get("/api/resume")).andExpect(jsonPath("$.topicId").value(SECOND));

        // Revisiting the finished ticket keeps its place but does not take over Continue.
        savePosition(FIRST, "{\"step\":1,\"furthest\":13,\"total\":14,\"label\":\"Hook\"}").andExpect(status().isOk());
        mvc.perform(get("/api/resume")).andExpect(jsonPath("$.topicId").value(SECOND));
    }

    @Test
    @DisplayName("A card outside the ticket or without a title is refused")
    void refusesBadPositions() throws Exception {
        savePosition(FIRST, "{\"step\":14,\"furthest\":14,\"total\":14,\"label\":\"Hook\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", Matchers.containsString("not part of this ticket")));
        savePosition(FIRST, "{\"step\":-1,\"furthest\":0,\"total\":14,\"label\":\"Hook\"}").andExpect(status().isBadRequest());
        savePosition(FIRST, "{\"step\":1,\"furthest\":1,\"total\":14,\"label\":\" \"}").andExpect(status().isBadRequest());
    }
}
