package dev.javaquest.progress;

/**
 * The read-aloud voice and speed.
 *
 * @param name the browser voice's name, such as "Daniel", or null for the browser's default voice. Names stay the
 *             same across browsers on one computer, unlike voice ids.
 * @param rate the speed, from {@link #MIN_RATE} (slower) to {@link #MAX_RATE} (faster); 1 is normal
 */
public record VoiceSettings(String name, double rate) {

    public static final double MIN_RATE = 0.5;
    public static final double MAX_RATE = 2.0;

    public void validate() {
        if (!(rate >= MIN_RATE && rate <= MAX_RATE)) {
            throw new IllegalArgumentException("Choose a speed from " + MIN_RATE + " to " + MAX_RATE + ".");
        }
        if (name != null && (name.isBlank() || name.length() > 200)) {
            throw new IllegalArgumentException("That voice name is not valid.");
        }
    }
}
