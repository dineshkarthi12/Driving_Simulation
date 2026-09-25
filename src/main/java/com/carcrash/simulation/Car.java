package com.carcrash.simulation;

import com.carcrash.domain.CarSpec;
import com.carcrash.domain.Command;
import com.carcrash.domain.Direction;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;

import java.util.List;
import java.util.Optional;

/**
 * Mutable state of one car during a single simulation run. The engine creates
 * fresh instances from the registered {@link CarSpec}s for every run, so the
 * specs are never modified and a simulation can be re-run safely.
 */
final class Car {

    private final CarSpec spec;
    private Position position;
    private Direction direction;
    private boolean finished;
    private CarOutcome.Collided collision;

    Car(CarSpec spec) {
        this.spec = spec;
        this.position = spec.start();
        this.direction = spec.direction();
    }

    String name() {
        return spec.name();
    }

    Position position() {
        return position;
    }

    /** Still has commands to execute and has not collided. */
    boolean isActive() {
        return !finished && !hasCollided();
    }

    boolean hasCollided() {
        return collision != null;
    }

    /** The command for the given 1-based step, or empty if the car has no command for it. */
    Optional<Command> commandAt(int step) {
        List<Command> commands = spec.commands();
        return step >= 1 && step <= commands.size()
                ? Optional.of(commands.get(step - 1))
                : Optional.empty();
    }

    /**
     * The cell the car would occupy after executing the command, without changing
     * the car. Rotations and forward moves that would leave the field keep the car in place.
     */
    Position plannedPosition(Command command, Field field) {
        if (command != Command.F) {
            return position;
        }
        Position target = position.moved(direction);
        return field.contains(target) ? target : position;
    }

    /** Executes a command; a forward move that would leave the field is ignored. */
    void execute(Command command, Field field) {
        switch (command) {
            case L -> direction = direction.turnLeft();
            case R -> direction = direction.turnRight();
            case F -> position = plannedPosition(command, field);
        }
    }

    /** Marks the car as having executed all of its commands; it stays on the field. */
    void finish() {
        finished = true;
    }

    /** Stops the car at its current cell. Both active and finished (parked) cars can be hit. */
    void crash(List<String> otherCars, int step) {
        collision = new CarOutcome.Collided(name(), otherCars, position, step);
    }

    CarOutcome outcome() {
        return hasCollided() ? collision : new CarOutcome.Finished(name(), position, direction);
    }
}
