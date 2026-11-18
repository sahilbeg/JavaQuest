package dev.cadence.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
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

/** The read-aloud voice and speed are kept with the progress, so a reload or another browser keeps them. */
@SpringBootTest
@AutoConfigureMockMvc
class VoiceSettingsTest {

    @Autowired
    MockMvc mvc;

    @DynamicPropertySource
    static void repository(DynamicPropertyRegistry registry) throws IOException {
        dev.cadence.TestRepository.register(registry, "voice-settings");
    }

    private org.springframework.test.web.servlet.ResultActions save(String json) throws Exception {
        return mvc.perform(put("/api/settings/voice").header(LocalOnlyFilter.WRITE_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    @DisplayName("A saved voice and speed come back on the next read")
    void savedVoiceComesBack() throws Exception {
        save("{\"name\":\"Daniel\",\"rate\":1.4}").andExpect(status().isOk());
        mvc.perform(get("/api/settings/voice"))
                .andExpect(jsonPath("$.name").value("Daniel"))
                .andExpect(jsonPath("$.rate").value(1.4));

        save("{\"name\":null,\"rate\":1.0}").andExpect(status().isOk())
                .andExpect(jsonPath("$.name").doesNotExist());
    }

    @Test
    @DisplayName("Speeds outside 0.5 to 2 and blank voice names are refused")
    void refusesBadValues() throws Exception {
        save("{\"name\":\"Daniel\",\"rate\":3}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", Matchers.containsString("speed")));
        save("{\"name\":\"Daniel\",\"rate\":0.2}").andExpect(status().isBadRequest());
        save("{\"name\":\"  \",\"rate\":1}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Browsers are told to check for newer app files, so new icons and scripts show up")
    void appFilesAreRevalidated() throws Exception {
        mvc.perform(get("/vendor/icons/icons.svg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", Matchers.containsString("no-cache")));
    }
}
