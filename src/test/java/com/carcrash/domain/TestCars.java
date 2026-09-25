package com.carcrash.domain;

/** Shared test fixture for building car specifications concisely. */
public final class TestCars {

    private TestCars() {
    }

    /** E.g. {@code car("A", 1, 2, "N", "FFR")}; an empty command string makes a parked car. */
    public static CarSpec car(String name, int x, int y, String direction, String commands) {
        return new CarSpec(name, new Position(x, y), Direction.fromSymbol(direction), Command.parseAll(commands));
    }
}
