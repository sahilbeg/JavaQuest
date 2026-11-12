package jq.m03.customexceptions;

public class UnknownBikeTypeException extends RuntimeException {

    public UnknownBikeTypeException(String type) {
        super("Unknown bike type: " + type);
    }
}
