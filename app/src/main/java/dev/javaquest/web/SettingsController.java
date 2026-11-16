package dev.javaquest.web;

import dev.javaquest.progress.ProgressService;
import dev.javaquest.progress.StudySettings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The learner's study settings: for now, the daily ticket limit. */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ProgressService progress;

    public SettingsController(ProgressService progress) {
        this.progress = progress;
    }

    @GetMapping
    public StudySettings settings() {
        return progress.settings();
    }

    @PutMapping
    public StudySettings save(@RequestBody StudySettings settings) {
        return progress.saveSettings(settings);
    }
}
