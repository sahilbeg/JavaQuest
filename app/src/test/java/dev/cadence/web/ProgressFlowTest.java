package dev.cadence.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Walks the hint and solution rules from docs/GAMIFICATION.md against a fresh database and the real hello-world topic. */
@SpringBootTest
@AutoConfigureMockMvc
class ProgressFlowTest {

    private static final String TOPIC = "/api/topics/module-00-foundations/topic-05-hello-world";

    @Autowired
    MockMvc mvc;

    static Path repo;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        repo = dev.cadence.TestRepository.register(registry, "progress-flow");
    }

    private ResultActions write(String path) throws Exception {
        return mvc.perform(post(TOPIC + path).header(LocalOnlyFilter.WRITE_HEADER, "1"));
    }

    private void failRuns(int times) throws Exception {
        for (int i = 0; i < times; i++) {
            write("/submit").andExpect(jsonPath("$.result.outcome").value("FAILED"));
        }
    }

    @Test
    void hintsAndSolutionUnlockAsChecksKeepFailing() throws Exception {
        write("/hints/next").andExpect(status().isOk())
                .andExpect(jsonPath("$.hint").value(Matchers.startsWith("Text in Java")))
                .andExpect(jsonPath("$.progress.hintsUsed").value(1));
        write("/hints/next").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("after 2 runs")));
        write("/solution").andExpect(status().isForbidden());

        failRuns(2);
        write("/hints/next").andExpect(status().isOk()).andExpect(jsonPath("$.progress.hintsUsed").value(2));
        write("/hints/next").andExpect(status().isForbidden());

        failRuns(2);
        write("/hints/next").andExpect(status().isOk()).andExpect(jsonPath("$.progress.hintsUsed").value(3));
        write("/solution").andExpect(status().isForbidden());
        write("/hints/next").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("every hint")));

        failRuns(1);
        write("/solution").andExpect(status().isOk())
                .andExpect(jsonPath("$.files[0].content").value(Matchers.containsString("return \"Hello, World!\"")))
                .andExpect(jsonPath("$.progress.solutionViewed").value(true));

        mvc.perform(get(TOPIC + "/hints")).andExpect(jsonPath("$.opened.length()").value(3));
        mvc.perform(get(TOPIC + "/progress"))
                .andExpect(jsonPath("$.latest.result.outcome").value("FAILED"))
                .andExpect(jsonPath("$.progress.failedRuns").value(5))
                .andExpect(jsonPath("$.progress.mastered").value(false));
    }

    @Test
    void resetPutsTheSkeletonBackIntoSrc() throws Exception {
        Path root = repo.resolve("practice/module-00-foundations/topic-05-hello-world");
        String file = "jq/m00/helloworld/Hello.java";
        String before = Files.readString(root.resolve("src").resolve(file));
        try {
            Files.writeString(root.resolve("src").resolve(file), "// learner edits");
            write("/reset").andExpect(status().isOk());
            org.junit.jupiter.api.Assertions.assertEquals(
                    Files.readString(root.resolve("skeleton").resolve(file)), Files.readString(root.resolve("src").resolve(file)));
        } finally {
            Files.writeString(root.resolve("src").resolve(file), before);
        }
    }
}
