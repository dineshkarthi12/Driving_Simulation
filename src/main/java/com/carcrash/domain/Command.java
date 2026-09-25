package com.carcrash.domain;

import java.util.List;
import java.util.stream.Collectors;

/** A single instruction that can be issued to a car. */
public enum Command {
    /** Rotate 90 degrees to the left. */
    L,
    /** Rotate 90 degrees to the right. */
    R,
    /** Move forward by one grid point. */
    F;

    /**
     * Parses a single command character, ignoring case.
     *
     * @throws IllegalArgumentException if the character is not L, R or F
     */
    public static Command fromChar(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'L' -> L;
            case 'R' -> R;
            case 'F' -> F;
            default -> throw new IllegalArgumentException(
                    "Invalid command '" + c + "'. Only L, R and F are allowed.");
        };
    }

    /**
     * Parses a command string such as {@code "FFRFF"}. Case is ignored and all
     * whitespace is skipped, so {@code "ff r f"} is accepted. An empty or blank
     * string yields an empty list (a parked car).
     *
     * @throws IllegalArgumentException if any non-whitespace character is not a valid command
     */
    public static List<Command> parseAll(String text) {
        return text.chars()
                .filter(c -> !Character.isWhitespace(c))
                .mapToObj(c -> fromChar((char) c))
                .toList();
    }

    /** Renders a command list back to its compact textual form, e.g. {@code "FFR"}. */
    public static String toText(List<Command> commands) {
        return commands.stream().map(Command::name).collect(Collectors.joining());
    }
}
