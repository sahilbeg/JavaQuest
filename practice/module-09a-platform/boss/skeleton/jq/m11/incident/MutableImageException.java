package jq.m11.incident;

/** Thrown when a deploy names an image that could point at a different build tomorrow. */
public class MutableImageException extends IllegalArgumentException {

    private final String image;

    public MutableImageException(String image, String reason) {
        super(image + " is not an immutable image reference: " + reason);
        this.image = image;
    }

    /** The image reference that was refused, exactly as it was given. */
    public String image() {
        return image;
    }
}
