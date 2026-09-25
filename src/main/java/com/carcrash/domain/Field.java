package com.carcrash.domain;

/**
 * Rectangular simulation field. A field of {@code width x height} has valid
 * coordinates {@code 0..width-1} horizontally and {@code 0..height-1} vertically,
 * so a 10 x 10 field has its top-right corner at (9, 9).
 */
public record Field(int width, int height) {

    public Field {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive whole numbers.");
        }
    }

    /** Whether the position lies within the field boundaries. */
    public boolean contains(Position position) {
        return position.x() >= 0 && position.x() < width
                && position.y() >= 0 && position.y() < height;
    }
}
