package jq.m11.dockerfiles;

/**
 * One Dockerfile instruction after continuation lines are joined.
 *
 * @param line    the 1-based line number where the instruction starts
 * @param keyword the instruction word in capitals, such as FROM, RUN or COPY
 * @param args    everything after the keyword, trimmed, with continuation lines joined by one space
 */
public record Instruction(int line, String keyword, String args) {
}
