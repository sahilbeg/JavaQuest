package dev.cadence.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TopicStatusTest {

    @Test
    void verifiedTopicMovesUpToTestsVerified() {
        assertEquals("tests_verified", TopicStatus.afterVerification("practice_written", true));
    }

    @Test
    void verificationNeverMovesATopicDown() {
        assertEquals("done", TopicStatus.afterVerification("done", true));
    }

    @Test
    void failedVerificationDropsAVerifiedTopicBackToPracticeWritten() {
        assertEquals("practice_written", TopicStatus.afterVerification("ui_wired", false));
    }

    @Test
    void failedVerificationLeavesAnEarlierTopicAlone() {
        assertEquals("content_written", TopicStatus.afterVerification("content_written", false));
    }

    @Test
    void unknownStatusIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> TopicStatus.afterVerification("finished", true));
    }
}
