package dev.cadence.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.cadence.content.ContentCatalog;
import dev.cadence.content.QuizSession;
import dev.cadence.progress.ProgressService;
import java.io.IOException;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Quick reference notes open once a ticket is finished, and stay open. */
@SpringBootTest
@AutoConfigureMockMvc
class NotesFlowTest {

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
        dev.cadence.TestRepository.register(registry, "notes-flow", false);
    }

    @Test
    @DisplayName("Notes open when the ticket is finished, not before")
    void notesOpenWhenTheTicketIsFinished() throws Exception {
        mvc.perform(get("/api/topics/" + FIRST + "/notes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", Matchers.containsString("once you finish this ticket")));

        progress.recordQuiz(catalog.topic(FIRST).orElseThrow(), 1, new QuizSession.Result(100, true, List.of(), List.of()));

        mvc.perform(get("/api/topics/" + FIRST + "/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyPointsHtml", Matchers.containsString("<li>")))
                .andExpect(jsonPath("$.syntax").isArray())
                .andExpect(jsonPath("$.wordsHtml").isArray())
                .andExpect(jsonPath("$.mistakes").isArray());

        // The next ticket is open now, but its notes wait until it is finished too.
        mvc.perform(get("/api/topics/" + SECOND)).andExpect(status().isOk());
        mvc.perform(get("/api/topics/" + SECOND + "/notes")).andExpect(status().isForbidden());
        mvc.perform(get("/api/topics/" + THIRD + "/notes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", Matchers.containsString("still locked")));
    }
}
