package com.carcrash.cli;

import com.carcrash.domain.Command;
import com.carcrash.domain.Direction;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;

import java.util.List;

/**
 * Converts raw text typed by the user into domain values. Input is trimmed,
 * tokens may be separated by any amount of whitespace, and letters are
 * case-insensitive. The parser only handles the shape of the input; value rules
 * (positive field size, known direction and commands) are enforced by the domain
 * types. Every failure is an {@link IllegalArgumentException} whose message is
 * meant for the user.
 */
public final class InputParser {

    private static final String WHITESPACE = "\\s+";

    /** Starting position and direction of a car, e.g. {@code 1 2 N}. */
    public record Placement(Position position, Direction direction) {
    }

    private InputParser() {
    }

    /** Parses {@code "width height"}, e.g. {@code "10 10"}. */
    public static Field parseField(String input) {
        String[] tokens = tokenize(input);
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Please enter exactly two numbers: width and height, e.g. 10 10.");
        }
        return new Field(parseInt(tokens[0], "Width"), parseInt(tokens[1], "Height"));
    }

    /** Parses {@code "x y Direction"}, e.g. {@code "1 2 N"}. */
    public static Placement parsePlacement(String input) {
        String[] tokens = tokenize(input);
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Please enter the position as x y Direction, e.g. 1 2 N.");
        }
        Position position = new Position(parseInt(tokens[0], "x"), parseInt(tokens[1], "y"));
        return new Placement(position, Direction.fromSymbol(tokens[2]));
    }

    /** Parses a command string such as {@code "FFRFF"}; blank input means no commands. */
    public static List<Command> parseCommands(String input) {
        return Command.parseAll(input);
    }

    /** Parses a menu choice between 1 and {@code optionCount}. */
    public static int parseMenuChoice(String input, int optionCount) {
        String trimmed = input.strip();
        try {
            int choice = Integer.parseInt(trimmed);
            if (choice >= 1 && choice <= optionCount) {
                return choice;
            }
        } catch (NumberFormatException e) {
            // Fall through to the common error message.
        }
        throw new IllegalArgumentException("Invalid option '" + trimmed + "'. Please enter a number from 1 to "
                + optionCount + ".");
    }

    private static String[] tokenize(String input) {
        String trimmed = input.strip();
        return trimmed.isEmpty() ? new String[0] : trimmed.split(WHITESPACE);
    }

    private static int parseInt(String token, String label) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number, but got '" + token + "'.");
        }
    }
}
