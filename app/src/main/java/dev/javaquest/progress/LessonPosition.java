package dev.javaquest.progress;

/**
 * Where the learner is inside a ticket's lesson player.
 *
 * @param step     the card on screen, from 0 (the ticket) up
 * @param furthest the furthest card reached, so the cards up to it can be jumped to again
 * @param total    how many cards the ticket had when this was saved
 * @param label    the card's title, which finds the right card again when a rebuild adds or removes cards
 */
public record LessonPosition(int step, int furthest, int total, String label) {

    public static final int MAX_CARDS = 200;

    public void validate() {
        if (step < 0 || furthest < 0 || total < 1 || step >= total || furthest >= total || total > MAX_CARDS) {
            throw new IllegalArgumentException("That card is not part of this ticket.");
        }
        if (label == null || label.isBlank() || label.length() > 300) {
            throw new IllegalArgumentException("A card needs a title of up to 300 characters.");
        }
    }
}
