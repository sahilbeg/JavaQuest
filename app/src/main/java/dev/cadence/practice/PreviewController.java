package dev.cadence.practice;

import dev.cadence.content.ContentCatalog;
import dev.cadence.content.Topic;
import dev.cadence.progress.Gate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The live preview of a web ticket's page. Opening the page builds src/ with the web workspace's preview tool
 * (copies the files, bundles main.js or main.jsx with React and CSS), then serves the result. The editor shows it in a
 * sandboxed frame, so the learner's script can't reach Cadence itself.
 */
@RestController
public class PreviewController {

    private static final Map<String, MediaType> TYPES = Map.of(
            "html", MediaType.TEXT_HTML, "css", MediaType.valueOf("text/css"), "js", MediaType.valueOf("text/javascript"),
            "jsx", MediaType.valueOf("text/javascript"), "json", MediaType.APPLICATION_JSON, "svg", MediaType.valueOf("image/svg+xml"),
            "png", MediaType.IMAGE_PNG, "jpg", MediaType.IMAGE_JPEG, "jpeg", MediaType.IMAGE_JPEG, "webp", MediaType.valueOf("image/webp"));

    private final ContentCatalog catalog;
    private final Gate gate;
    private final Runners runners;
    private final Map<String, Path> builds = new ConcurrentHashMap<>();

    public PreviewController(ContentCatalog catalog, Gate gate, Runners runners) {
        this.catalog = catalog;
        this.gate = gate;
        this.runners = runners;
    }

    @GetMapping("/preview/{module}/{topic}/")
    public ResponseEntity<byte[]> page(@PathVariable String module, @PathVariable String topic) throws IOException, InterruptedException {
        Topic found = open(module, topic);
        var node = runners.node().orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "The web tools aren't installed yet. Restart Cadence with ./run.sh, which installs them."));
        Path out = builds.computeIfAbsent(found.id(), id -> node.workspace().resolve(".runs/preview-" + UUID.randomUUID()));
        var build = new ProcessBuilder(List.of("node", node.workspace().resolve("tools/preview.mjs").toString(),
                found.practiceSources().toString(), out.toString()))
                .redirectErrorStream(true).start();
        if (!build.waitFor(20, TimeUnit.SECONDS)) {
            build.destroyForcibly();
            return error("The page took too long to build.");
        }
        String report = new String(build.getInputStream().readAllBytes());
        if (build.exitValue() != 0) {
            return error("The page doesn't build yet:\n\n" + report);
        }
        return file(out, "index.html");
    }

    @GetMapping("/preview/{module}/{topic}/{file:[A-Za-z0-9._-]+}")
    public ResponseEntity<byte[]> asset(@PathVariable String module, @PathVariable String topic, @PathVariable String file) throws IOException {
        Topic found = open(module, topic);
        Path out = builds.get(found.id());
        if (out == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Open the preview first");
        }
        return file(out, file);
    }

    private ResponseEntity<byte[]> file(Path out, String name) throws IOException {
        Path file = out.resolve(name).normalize();
        if (!file.startsWith(out) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No file " + name);
        }
        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        return ResponseEntity.ok()
                .contentType(TYPES.getOrDefault(ext, MediaType.APPLICATION_OCTET_STREAM))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                // The frame is sandboxed (an opaque origin), so its module script is a cross-origin request.
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .body(Files.readAllBytes(file));
    }

    private static ResponseEntity<byte[]> error(String message) {
        String html = "<!doctype html><meta charset=utf-8><body style=\"font:14px/1.5 ui-monospace,monospace;padding:16px;white-space:pre-wrap\">"
                + message.replace("&", "&amp;").replace("<", "&lt;") + "</body>";
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).header(HttpHeaders.CACHE_CONTROL, "no-store").body(html.getBytes());
    }

    private Topic open(String module, String topic) {
        Topic found = catalog.topic(module + "/" + topic)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No topic " + module + "/" + topic));
        if (!found.meta().practice().preview()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This ticket has no page to preview");
        }
        return gate.requireOpen(found);
    }
}
