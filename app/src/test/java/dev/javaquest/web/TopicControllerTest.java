package dev.javaquest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Runs against the real content/ and practice/ folders of this repository. */
@SpringBootTest
@AutoConfigureMockMvc
class TopicControllerTest {

    private static final String HELLO = "/api/topics/module-00-foundations/topic-05-hello-world/submit";

    @Autowired
    MockMvc mvc;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        Path classpathFile = Files.createTempFile("runner-classpath", ".txt");
        Files.writeString(classpathFile, System.getProperty("junit.console.jar"));
        registry.add("javaquest.root", () -> Path.of("..").toAbsolutePath().normalize().toString());
        registry.add("javaquest.runner.classpath-file", classpathFile::toString);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:controller-test;DB_CLOSE_DELAY=-1");
    }

    @Test
    void catalogListsTheSampleTopic() throws Exception {
        mvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topics[?(@.id == 'module-00-foundations/topic-05-hello-world')].practiceKind")
                        .value("CODE"));
    }

    @Test
    void submittingTheUntouchedSkeletonFailsBothTests() throws Exception {
        mvc.perform(post(HELLO).header(LocalOnlyFilter.WRITE_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.outcome").value("FAILED"))
                .andExpect(jsonPath("$.result.tests.length()").value(2))
                .andExpect(jsonPath("$.source").value("BROWSER"));
    }

    @Test
    void submitWithoutTheWriteHeaderIsRefused() throws Exception {
        mvc.perform(post(HELLO)).andExpect(status().isForbidden());
    }

    @Test
    void requestForAnotherHostIsRefused() throws Exception {
        mvc.perform(get("/api/catalog").header("Host", "evil.example")).andExpect(status().isForbidden());
    }

    @Test
    void unknownTopicIsNotFound() throws Exception {
        mvc.perform(post("/api/topics/module-99-nope/topic-01-x/submit").header(LocalOnlyFilter.WRITE_HEADER, "1"))
                .andExpect(status().isNotFound());
    }

    private static final String TOPIC = "/api/topics/module-00-foundations/topic-05-hello-world";
    private static final String SOURCE = "jq/m00/helloworld/Hello.java";

    @Test
    void topicDetailCarriesTheTicketLessonAndNeighbours() throws Exception {
        mvc.perform(get(TOPIC))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.ticket.key").value("SPK-0005"))
                .andExpect(jsonPath("$.summary.ticket.from").value("maya"))
                .andExpect(jsonPath("$.lessonHtml").value(org.hamcrest.Matchers.containsString("data-runnable")))
                .andExpect(jsonPath("$.hintCount").value(3))
                .andExpect(jsonPath("$.sourceFiles[0]").value(SOURCE));
    }

    @Test
    void quizAttemptHidesAnswersAndGradesBySeed() throws Exception {
        mvc.perform(get(TOPIC + "/quiz").param("seed", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seed").value(11))
                .andExpect(jsonPath("$.questions.length()").value(6))
                .andExpect(jsonPath("$.questions[0].answer").doesNotExist())
                .andExpect(jsonPath("$.questions[0].explain").doesNotExist());

        mvc.perform(post(TOPIC + "/quiz").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType("application/json").content("{\"seed\": 11, \"answers\": {}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.scorePercent").value(0))
                .andExpect(jsonPath("$.result.passed").value(false))
                .andExpect(jsonPath("$.result.questions[0].explain").exists())
                .andExpect(jsonPath("$.progress.quizAttempts").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void sourceCanBeReadAndSavedBack() throws Exception {
        String body = mvc.perform(get(TOPIC + "/source").param("path", SOURCE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(put(TOPIC + "/source").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());
    }

    @Test
    void savingOutsideTheTopicSourcesIsRefused() throws Exception {
        mvc.perform(put(TOPIC + "/source").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType("application/json")
                        .content("{\"path\": \"../test/jq/m00/helloworld/HelloTest.java\", \"content\": \"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Not a practice source file")));
    }

    @Test
    void runningTheSkeletonCompletesWithNoOutput() throws Exception {
        mvc.perform(post(TOPIC + "/run").header(LocalOnlyFilter.WRITE_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result.output").value(""));
    }

    @Test
    void snippetCompileErrorComesWithAPlainExplanation() throws Exception {
        mvc.perform(post("/api/snippets/run").header(LocalOnlyFilter.WRITE_HEADER, "1")
                        .contentType("application/json")
                        .content("{\"code\": \"public class Main { public static void main(String[] a) { System.out.println(1) } }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("COMPILE_ERROR"))
                .andExpect(jsonPath("$.explanations[0]").value(org.hamcrest.Matchers.containsString("expected a ;")));
    }

    @Test
    void randomQuizSeedsSurviveAJavaScriptNumber() throws Exception {
        for (int i = 0; i < 20; i++) {
            String body = mvc.perform(get(TOPIC + "/quiz")).andReturn().getResponse().getContentAsString();
            long seed = Long.parseLong(body.replaceAll(".*\"seed\":(\\d+).*", "$1"));
            org.junit.jupiter.api.Assertions.assertTrue(seed <= TopicController.MAX_BROWSER_SAFE_SEED, "seed " + seed);
            org.junit.jupiter.api.Assertions.assertEquals(seed, (long) (double) seed);
        }
    }
}
