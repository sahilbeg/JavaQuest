package dev.javaquest.web;

import dev.javaquest.progress.Gate;
import dev.javaquest.progress.ProgressService;
import dev.javaquest.progress.StudySettings;
import dev.javaquest.progress.VoiceSettings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The learner's study settings: the daily ticket limit, with today's count, for the control in the top bar. */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    /** The saved settings plus today's pace: tickets completed today and whether the limit is reached. */
    public record View(boolean dailyLimitOn, int dailyLimit, int doneToday, boolean reached, boolean authorMode) {}

    private final ProgressService progress;
    private final Gate gate;

    public SettingsController(ProgressService progress, Gate gate) {
        this.progress = progress;
        this.gate = gate;
    }

    @GetMapping
    public View settings() {
        Gate.Today today = gate.today();
        return new View(today.limitOn(), today.limit(), today.doneToday(), today.reached(), gate.authorMode());
    }

    @PutMapping
    public View save(@RequestBody StudySettings settings) {
        progress.saveSettings(settings);
        return settings();
    }

    /** The read-aloud voice and speed, kept with the progress so every browser uses the same ones. */
    @GetMapping("/voice")
    public VoiceSettings voice() {
        return progress.voice();
    }

    @PutMapping("/voice")
    public VoiceSettings saveVoice(@RequestBody VoiceSettings voice) {
        return progress.saveVoice(voice);
    }
}
