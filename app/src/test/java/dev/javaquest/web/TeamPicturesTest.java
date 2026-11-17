package dev.javaquest.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Team pictures come from the learner's own avatars/ folder: only plain image files, never anything else. */
@SpringBootTest
@AutoConfigureMockMvc
class TeamPicturesTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 0};

    @Autowired
    MockMvc mvc;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        Path root = dev.javaquest.TestRepository.register(registry, "team-pictures");
        Path avatars = Files.createDirectories(root.resolve("avatars"));
        Files.write(avatars.resolve("tsunade.png"), PNG);
        Files.write(avatars.resolve("kakashi.jpg"), PNG);
        Files.writeString(avatars.resolve("README.md"), "# Team pictures");
        Files.writeString(avatars.resolve("evil.svg"), "<svg onload=alert(1)/>");
        Files.writeString(root.resolve("secret.png"), "outside the folder");
    }

    @Test
    @DisplayName("The list holds only the picture files in avatars/")
    void listsOnlyPictures() throws Exception {
        mvc.perform(get("/api/team/avatars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("kakashi.jpg"))
                .andExpect(jsonPath("$[1]").value("tsunade.png"));
    }

    @Test
    @DisplayName("A picture is served with its image type")
    void servesAPicture() throws Exception {
        mvc.perform(get("/api/team/avatars/tsunade.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(PNG));
        mvc.perform(get("/api/team/avatars/kakashi.jpg"))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    @DisplayName("SVG, other files, missing pictures and paths outside the folder are refused")
    void refusesEverythingElse() throws Exception {
        mvc.perform(get("/api/team/avatars/evil.svg")).andExpect(status().isNotFound());
        mvc.perform(get("/api/team/avatars/README.md")).andExpect(status().isNotFound());
        mvc.perform(get("/api/team/avatars/shizune.png")).andExpect(status().isNotFound());
        mvc.perform(get("/api/team/avatars/..%2Fsecret.png")).andExpect(status().isNotFound());
    }
}
