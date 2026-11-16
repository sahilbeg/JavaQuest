package dev.javaquest.progress;

import dev.javaquest.JavaQuestProperties;
import dev.javaquest.content.ContentCatalog;
import dev.javaquest.content.Topic;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Tickets open one at a time, in curriculum order. A ticket is open when it is mastered, or when every ticket
 * before it is mastered. The first ticket that is not mastered is the learner's frontier: everything after it
 * stays locked, except tickets mastered earlier, which stay open for practice. Author mode opens everything.
 */
@Service
public class Gate {

    public record Access(boolean locked, String frontierId) {}

    private final ContentCatalog catalog;
    private final ProgressService progress;
    private final boolean unlockAll;

    public Gate(ContentCatalog catalog, ProgressService progress, JavaQuestProperties properties) {
        this.catalog = catalog;
        this.progress = progress;
        this.unlockAll = properties.unlockAll();
    }

    public boolean authorMode() {
        return unlockAll;
    }

    /** Access for every ticket, keyed by id, in curriculum order. */
    public Map<String, Access> accessAll() {
        List<Topic> topics = catalog.topics();
        var mastered = new boolean[topics.size()];
        int frontier = topics.size();
        for (int i = 0; i < topics.size(); i++) {
            mastered[i] = progress.progress(topics.get(i)).mastered();
            if (!mastered[i] && frontier == topics.size()) {
                frontier = i;
            }
        }
        String frontierId = frontier < topics.size() ? topics.get(frontier).id() : null;
        var access = new LinkedHashMap<String, Access>();
        for (int i = 0; i < topics.size(); i++) {
            boolean locked = !unlockAll && !mastered[i] && i > frontier;
            access.put(topics.get(i).id(), new Access(locked, frontierId));
        }
        return access;
    }

    public Access access(Topic topic) {
        return accessAll().get(topic.id());
    }

    /** Throws LockedException, which the API answers with 403, when the learner has not reached this ticket yet. */
    public Topic requireOpen(Topic topic) {
        Access access = access(topic);
        if (access.locked()) {
            String frontier = catalog.topic(access.frontierId())
                    .map(t -> t.meta().ticket().key() + " \"" + t.meta().ticket().title() + "\"")
                    .orElse("the ticket before it");
            throw new LockedException("This ticket is still locked. Finish " + frontier
                    + " first: tickets open one at a time.");
        }
        return topic;
    }
}
