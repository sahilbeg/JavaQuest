package dev.javaquest.web;

import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.ModuleInfo;
import dev.javaquest.content.Topic;
import dev.javaquest.practice.PracticeResult;
import dev.javaquest.practice.PracticeResult.Source;
import dev.javaquest.practice.PracticeService;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class TopicController {

    public record TopicSummary(String id, String title, String practiceKind, PracticeResult latest) {}

    public record Catalog(List<ModuleInfo> modules, List<TopicSummary> topics) {}

    private final ContentCatalog catalog;
    private final PracticeService practice;
    private final EventHub events;

    public TopicController(ContentCatalog catalog, PracticeService practice, EventHub events) {
        this.catalog = catalog;
        this.practice = practice;
        this.events = events;
    }

    @GetMapping("/catalog")
    public Catalog catalog() {
        var topics = catalog.topics().stream()
                .map(t -> new TopicSummary(t.id(), t.meta().title(), t.meta().practice().kind().name(),
                        practice.latest(t.id()).orElse(null)))
                .toList();
        return new Catalog(catalog.modules(), topics);
    }

    @PostMapping("/topics/{module}/{topic}/submit")
    public PracticeResult submit(@PathVariable String module, @PathVariable String topic)
            throws IOException, InterruptedException {
        return practice.submit(find(module + "/" + topic), Source.BROWSER);
    }

    @GetMapping(path = "/events", produces = "text/event-stream")
    public SseEmitter events() {
        return events.subscribe();
    }

    private Topic find(String id) {
        return catalog.topic(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No topic " + id));
    }
}
