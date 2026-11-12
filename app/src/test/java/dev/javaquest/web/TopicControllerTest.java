package dev.javaquest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
