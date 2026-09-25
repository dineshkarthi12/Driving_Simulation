package com.carcrash.domain;

import java.util.Locale;

/**
 * Compass direction a car can face. Declared in clockwise order so that
 * rotation is simple index arithmetic.
 */
public enum Direction {
    N(0, 1),
    E(1, 0),
    S(0, -1),
    W(-1, 0);

    private static final Direction[] CLOCKWISE = values();

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /** Horizontal delta of a single forward move. */
    public int dx() {
        return dx;
    }

    /** Vertical delta of a single forward move. */
    public int dy() {
        return dy;
    }

    /** Direction after rotating 90 degrees counter-clockwise. */
    public Direction turnLeft() {
        return CLOCKWISE[(ordinal() + CLOCKWISE.length - 1) % CLOCKWISE.length];
    }

    /** Direction after rotating 90 degrees clockwise. */
    public Direction turnRight() {
        return CLOCKWISE[(ordinal() + 1) % CLOCKWISE.length];
    }

    /**
     * Parses a single-letter direction symbol (N, E, S or W), ignoring case and
     * surrounding whitespace.
     *
     * @throws IllegalArgumentException if the symbol is not a known direction
     */
    public static Direction fromSymbol(String symbol) {
        try {
            return valueOf(symbol.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid direction '" + symbol + "'. Use one of N, E, S, W.", e);
        }
    }
}
