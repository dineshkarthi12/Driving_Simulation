package com.carcrash.cli;

import com.carcrash.domain.CarSpec;
import com.carcrash.domain.Command;
import com.carcrash.simulation.CarOutcome;

/** Formats domain objects into the textual lines shown to the user. */
public final class OutputFormatter {

    static final String NO_COMMANDS = "(no commands)";

    private OutputFormatter() {
    }

    /** E.g. {@code - A, (1,2) N, FFRFFFFRRL} or {@code - P, (3,3) E, (no commands)}. */
    public static String formatCar(CarSpec car) {
        String commands = car.isParked() ? NO_COMMANDS : Command.toText(car.commands());
        return "- " + car.name() + ", " + car.start() + " " + car.direction() + ", " + commands;
    }

    /** E.g. {@code - A, (5,4) S} or {@code - A, collides with B at (5,4) at step 7}. */
    public static String formatOutcome(CarOutcome outcome) {
        return switch (outcome) {
            case CarOutcome.Finished f -> "- " + f.name() + ", " + f.position() + " " + f.direction();
            case CarOutcome.Collided c -> "- " + c.name() + ", collides with " + String.join(", ", c.otherCars())
                    + " at " + c.position() + " at step " + c.step();
        };
    }
}
