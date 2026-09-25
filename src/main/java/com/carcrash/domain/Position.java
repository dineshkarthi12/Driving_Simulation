package com.carcrash.domain;

/** Immutable grid coordinate. (0, 0) is the bottom-left corner of the field. */
public record Position(int x, int y) {

    /** The position one step away in the given direction. */
    public Position moved(Direction direction) {
        return new Position(x + direction.dx(), y + direction.dy());
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
