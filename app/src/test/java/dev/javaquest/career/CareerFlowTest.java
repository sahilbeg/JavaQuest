package dev.javaquest.career;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.QuizSession;
import dev.javaquest.progress.ProgressService;
import dev.javaquest.web.LocalOnlyFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** One learner closing the hello-world ticket, checked through the career summary. */
@SpringBootTest
@AutoConfigureMockMvc
class CareerFlowTest {

    private static final String TOPIC_ID = "module-00-foundations/topic-05-hello-world";
    private static final String TOPIC = "/api/topics/" + TOPIC_ID;
    private static final Path PRACTICE = Path.of("..").toAbsolutePath().normalize().resolve("practice").resolve(TOPIC_ID);
    private static final String FILE = "jq/m00/helloworld/Hello.java";

    @Autowired
    MockMvc mvc;

    @Autowired
    ProgressService progress;

    @Autowired
    ContentCatalog catalog;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        Path classpathFile = Files.createTempFile("runner-classpath", ".txt");
        Files.writeString(classpathFile, System.getProperty("junit.console.jar"));
        registry.add("javaquest.root", () -> Path.of("..").toAbsolutePath().normalize().toString());
        registry.add("javaquest.runner.classpath-file", classpathFile::toString);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:career-flow;DB_CLOSE_DELAY=-1");
    }

    @Test
    void closingTheFirstTicketEarnsXpKudosAndAReviewCard() throws Exception {
        mvc.perform(post(TOPIC + "/read").header(LocalOnlyFilter.WRITE_HEADER, "1")).andExpect(status().isOk());
        mvc.perform(post(TOPIC + "/read").header(LocalOnlyFilter.WRITE_HEADER, "1")).andExpect(status().isOk());
        mvc.perform(get("/api/career"))
                .andExpect(jsonPath("$.level.xp").value(10 + 5))
                .andExpect(jsonPath("$.streak.current").value(1))
                .andExpect(jsonPath("$.streak.activeToday").value(true));

        String skeleton = Files.readString(PRACTICE.resolve("src").resolve(FILE));
        try {
            Files.copy(PRACTICE.resolve("solution").resolve(FILE), PRACTICE.resolve("src").resolve(FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            mvc.perform(post(TOPIC + "/submit").header(LocalOnlyFilter.WRITE_HEADER, "1"))
                    .andExpect(jsonPath("$.result.outcome").value("PASSED"));
            mvc.perform(post(TOPIC + "/submit").header(LocalOnlyFilter.WRITE_HEADER, "1"));
        } finally {
            Files.writeString(PRACTICE.resolve("src").resolve(FILE), skeleton);
        }
        mvc.perform(get("/api/career")).andExpect(jsonPath("$.level.xp").value(15 + 100 + 25));

        var topic = catalog.topic(TOPIC_ID).orElseThrow();
        progress.recordQuiz(topic, 1, new QuizSession.Result(100, true, List.of(), List.of()));

        mvc.perform(get("/api/career"))
                .andExpect(jsonPath("$.level.xp").value(140 + 50 + 20))
                .andExpect(jsonPath("$.level.level").value(2))
                .andExpect(jsonPath("$.level.title").value("Intern"))
                .andExpect(jsonPath("$.level.nextTitle").value("Junior Developer"))
                .andExpect(jsonPath("$.kudos[?(@.id == 'hello-world')].earned").value(Matchers.contains(true)))
                .andExpect(jsonPath("$.kudos[?(@.id == 'first-green')].earned").value(Matchers.contains(true)))
                .andExpect(jsonPath("$.kudos[?(@.id == 'shipped-module-00-foundations')].earned").value(Matchers.contains(false)))
                .andExpect(jsonPath("$.topics[0].masteryPercent").value(100))
                .andExpect(jsonPath("$.reviewsDue.length()").value(0));

        String export = mvc.perform(get("/api/progress/export")).andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/progress/import").header(LocalOnlyFilter.WRITE_HEADER, "1")
                .contentType("application/json").content(export)).andExpect(status().isOk());
        mvc.perform(get("/api/career"))
                .andExpect(jsonPath("$.level.xp").value(210))
                .andExpect(jsonPath("$.streak.activeToday").value(true))
                .andExpect(jsonPath("$.reviewsDue.length()").value(0))
                .andExpect(jsonPath("$.kudos[?(@.id == 'hello-world')].earned").value(Matchers.contains(true)));
        org.junit.jupiter.api.Assertions.assertTrue(export.contains("\"LAST_ACTIVE\":\"" + java.time.LocalDate.now() + "\""),
                "dates must export as plain calendar dates");
    }

    @Test
    void importRejectsAFileThatIsNotAnExport() throws Exception {
        mvc.perform(post("/api/progress/import").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType("application/json").content("{\"version\": 7, \"tables\": {}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("not a JavaQuest progress export")));
    }
}
