package com.carcrash.domain;

import java.util.List;
import java.util.Objects;

/**
 * Immutable description of a car as registered by the user: its unique name,
 * starting position and direction, and the commands it will execute.
 * An empty command list denotes a parked car.
 */
public record CarSpec(String name, Position start, Direction direction, List<Command> commands) {

    public CarSpec {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(commands, "commands");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Car name must not be empty.");
        }
        commands = List.copyOf(commands);
    }

    /** Whether this car has no commands and therefore never moves. */
    public boolean isParked() {
        return commands.isEmpty();
    }
}
