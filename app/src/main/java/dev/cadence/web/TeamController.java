package dev.cadence.web;

import dev.cadence.CadenceProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Profile pictures for the team, from the learner's own avatars/ folder next to content/ (kept out of git). A file
 * named after a teammate, such as tsunade.png or maya.png, replaces that teammate's initials in the app.
 */
@RestController
@RequestMapping("/api/team/avatars")
public class TeamController {

    /** Plain image names only: no folders, no dots before the extension, and no SVG, which can carry scripts. */
    private static final Pattern NAME = Pattern.compile("[a-z0-9_-]{1,40}\\.(png|jpe?g|webp|gif)");
    private static final Map<String, MediaType> TYPES = Map.of(
            "png", MediaType.IMAGE_PNG, "jpg", MediaType.IMAGE_JPEG, "jpeg", MediaType.IMAGE_JPEG,
            "gif", MediaType.IMAGE_GIF, "webp", MediaType.parseMediaType("image/webp"));
    private static final long MAX_BYTES = 2 * 1024 * 1024;

    private final Path folder;

    public TeamController(CadenceProperties properties) {
        this.folder = properties.avatarsDir();
    }

    /** The picture files that are there, such as ["kakashi.jpg", "tsunade.png"]. Empty when there is no folder. */
    @GetMapping
    public List<String> list() throws IOException {
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString().toLowerCase())
                    .filter(name -> NAME.matcher(name).matches())
                    .sorted()
                    .toList();
        }
    }

    @GetMapping("/{file}")
    public ResponseEntity<byte[]> picture(@PathVariable String file) throws IOException {
        String name = file.toLowerCase();
        if (!NAME.matcher(name).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such picture");
        }
        Path path = folder.resolve(name);
        if (!Files.isRegularFile(path) || !path.normalize().startsWith(folder.normalize())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such picture");
        }
        if (Files.size(path) > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, name + " is over 2 MB. Use a smaller picture.");
        }
        String extension = name.substring(name.lastIndexOf('.') + 1);
        return ResponseEntity.ok()
                .contentType(TYPES.get(extension))
                .cacheControl(CacheControl.noCache())
                .header("X-Content-Type-Options", "nosniff")
                .body(Files.readAllBytes(path));
    }
}
