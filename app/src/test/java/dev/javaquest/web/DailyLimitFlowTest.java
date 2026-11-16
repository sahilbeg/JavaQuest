package dev.javaquest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** With the daily limit on, the next new ticket waits for tomorrow; finished tickets stay open for revisiting. */
@SpringBootTest
@AutoConfigureMockMvc
class DailyLimitFlowTest {

    private static final String FIRST = "module-00-foundations/topic-01-how-computers-work";
    private static final String SECOND = "module-00-foundations/topic-02-what-is-a-program";
    private static final String THIRD = "module-00-foundations/topic-03-terminal-basics";

    @Autowired
    MockMvc mvc;

    @Autowired
    ProgressService progress;

    @Autowired
    ContentCatalog catalog;

    @Autowired
    JdbcClient jdbc;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        dev.javaquest.TestRepository.register(registry, "daily-limit-flow", false);
    }

    private void saveSettings(String json) throws Exception {
        mvc.perform(put("/api/settings").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    private void master(String id) {
        progress.recordQuiz(catalog.topic(id).orElseThrow(), 1, new QuizSession.Result(100, true, List.of(), List.of()));
    }

    @Test
    void theNextTicketWaitsForTomorrowOnceTheDailyLimitIsReached() throws Exception {
        mvc.perform(get("/api/settings"))
                .andExpect(jsonPath("$.dailyLimitOn").value(false))
                .andExpect(jsonPath("$.dailyLimit").value(3));

        saveSettings("{\"dailyLimitOn\":true,\"dailyLimit\":2}");
        master(FIRST);
        mvc.perform(get("/api/catalog"))
                .andExpect(jsonPath("$.today.limitOn").value(true))
                .andExpect(jsonPath("$.today.doneToday").value(1))
                .andExpect(jsonPath("$.today.reached").value(false))
                .andExpect(jsonPath("$.topics[1].locked").value(false));

        master(SECOND);
        mvc.perform(get("/api/catalog"))
                .andExpect(jsonPath("$.frontierId").value(THIRD))
                .andExpect(jsonPath("$.today.doneToday").value(2))
                .andExpect(jsonPath("$.today.reached").value(true))
                .andExpect(jsonPath("$.topics[2].locked").value(true));
        mvc.perform(get("/api/topics/" + THIRD))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", Matchers.containsString("opens tomorrow")));

        // Finished tickets, their quiz and their review stay open for revisiting.
        mvc.perform(get("/api/topics/" + FIRST)).andExpect(status().isOk());
        mvc.perform(get("/api/topics/" + SECOND + "/quiz")).andExpect(status().isOk());
        mvc.perform(get("/api/reviews/" + FIRST)).andExpect(status().isOk());

        // A new day: today's count starts again from zero, so the next ticket opens.
        jdbc.sql("UPDATE topic_progress SET mastered_at = DATEADD('DAY', -1, mastered_at)").update();
        mvc.perform(get("/api/catalog"))
                .andExpect(jsonPath("$.today.doneToday").value(0))
                .andExpect(jsonPath("$.today.reached").value(false))
                .andExpect(jsonPath("$.topics[2].locked").value(false));
        mvc.perform(get("/api/topics/" + THIRD)).andExpect(status().isOk());

        // Back to today: turning the limit off opens the next ticket straight away.
        jdbc.sql("UPDATE topic_progress SET mastered_at = DATEADD('DAY', 1, mastered_at)").update();
        mvc.perform(get("/api/topics/" + THIRD)).andExpect(status().isForbidden());
        saveSettings("{\"dailyLimitOn\":false,\"dailyLimit\":2}");
        mvc.perform(get("/api/topics/" + THIRD)).andExpect(status().isOk());
        mvc.perform(get("/api/catalog")).andExpect(jsonPath("$.today.reached").value(false));
    }

    @Test
    void theLimitMustBeBetweenOneAndTwenty() throws Exception {
        for (String limit : List.of("0", "21")) {
            mvc.perform(put("/api/settings").header(LocalOnlyFilter.WRITE_HEADER, "1")
                            .contentType(MediaType.APPLICATION_JSON).content("{\"dailyLimitOn\":true,\"dailyLimit\":" + limit + "}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", Matchers.containsString("between 1 and 20")));
        }
        mvc.perform(put("/api/settings").contentType(MediaType.APPLICATION_JSON).content("{\"dailyLimitOn\":true,\"dailyLimit\":2}"))
                .andExpect(status().isForbidden());
    }
}
