package dev.cadence.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Page addresses are real paths, so they can be bookmarked and reloaded: each one serves the single page. */
@SpringBootTest
@AutoConfigureMockMvc
class PageRoutesTest {

    @Autowired
    MockMvc mvc;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        dev.cadence.TestRepository.register(registry, "page-routes");
    }

    @Test
    @DisplayName("Every page path answers with the app page")
    void pagePathsServeTheApp() throws Exception {
        for (String path : List.of("/t/module-01-basics/topic-01-variables", "/t/module-00-foundations/boss",
                "/review/module-01-basics/topic-01-variables", "/notes", "/notes/module-01-basics/topic-01-variables",
                "/career", "/offer")) {
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test
    @DisplayName("Unknown paths and the API are not turned into pages")
    void otherPathsAreNotPages() throws Exception {
        mvc.perform(get("/no-such-page")).andExpect(status().isNotFound());
        mvc.perform(get("/t/only-one-part")).andExpect(status().isNotFound());
        mvc.perform(get("/api/catalog")).andExpect(status().isOk()).andExpect(forwardedUrl(null));
    }
}
