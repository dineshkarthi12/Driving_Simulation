package com.carcrash.cli;

import com.carcrash.domain.Command;
import com.carcrash.domain.Direction;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;

import java.util.List;

/**
 * Converts raw text typed by the user into domain values. Input is trimmed,
 * tokens may be separated by any amount of whitespace, and letters are
 * case-insensitive. Every failure is reported as an {@link InvalidInputException}
 * with a user-friendly message.
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
            throw new InvalidInputException("Please enter exactly two numbers: width and height, e.g. 10 10.");
        }
        int width = parseInt(tokens[0], "Width");
        int height = parseInt(tokens[1], "Height");
        if (width <= 0 || height <= 0) {
            throw new InvalidInputException("Width and height must be positive whole numbers.");
        }
        return new Field(width, height);
    }

    /** Parses {@code "x y Direction"}, e.g. {@code "1 2 N"}. */
    public static Placement parsePlacement(String input) {
        String[] tokens = tokenize(input);
        if (tokens.length != 3) {
            throw new InvalidInputException("Please enter the position as x y Direction, e.g. 1 2 N.");
        }
        int x = parseInt(tokens[0], "x");
        int y = parseInt(tokens[1], "y");
        Direction direction;
        try {
            direction = Direction.fromSymbol(tokens[2]);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(e.getMessage());
        }
        return new Placement(new Position(x, y), direction);
    }

    /** Parses a command string such as {@code "FFRFF"}; blank input means no commands. */
    public static List<Command> parseCommands(String input) {
        try {
            return Command.parseAll(input);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }

    /** Parses a car name. Surrounding whitespace is removed. */
    public static String parseName(String input) {
        String name = input == null ? "" : input.strip();
        if (name.isEmpty()) {
            throw new InvalidInputException("Car name must not be empty.");
        }
        return name;
    }

    /** Parses a menu choice between 1 and {@code optionCount}. */
    public static int parseMenuChoice(String input, int optionCount) {
        String trimmed = input == null ? "" : input.strip();
        try {
            int choice = Integer.parseInt(trimmed);
            if (choice >= 1 && choice <= optionCount) {
                return choice;
            }
        } catch (NumberFormatException e) {
            // Fall through to the common error message.
        }
        throw new InvalidInputException("Invalid option '" + trimmed + "'. Please enter a number from 1 to "
                + optionCount + ".");
    }

    private static String[] tokenize(String input) {
        String trimmed = input == null ? "" : input.strip();
        return trimmed.isEmpty() ? new String[0] : trimmed.split(WHITESPACE);
    }

    private static int parseInt(String token, String label) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(label + " must be a whole number, but got '" + token + "'.");
        }
    }
}
