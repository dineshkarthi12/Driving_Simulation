package com.carcrash.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable runtime state of a single car during a simulation run. Instances are
 * created fresh from a {@link CarSpec} for every run, so the registered specs
 * are never modified and a simulation can be re-run safely.
 */
public final class Car {

    /** Lifecycle of a car within a simulation run. */
    public enum Status {
        /** Still has commands left to execute. */
        ACTIVE,
        /** Ran out of commands; stays on the field and can still be hit. */
        FINISHED,
        /** Involved in a collision; ignores all further commands. */
        COLLIDED
    }

    /** Details of the collision that stopped this car. */
    public record Collision(List<String> otherCars, Position position, int step) {
        public Collision {
            otherCars = List.copyOf(otherCars);
        }
    }

    private final CarSpec spec;
    private Position position;
    private Direction direction;
    private Status status = Status.ACTIVE;
    private Collision collision;

    public Car(CarSpec spec) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.position = spec.start();
        this.direction = spec.direction();
    }

    public String name() {
        return spec.name();
    }

    public Position position() {
        return position;
    }

    public Direction direction() {
        return direction;
    }

    public Status status() {
        return status;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean hasCollided() {
        return status == Status.COLLIDED;
    }

    /** The collision that stopped this car, if any. */
    public Optional<Collision> collision() {
        return Optional.ofNullable(collision);
    }

    /** The command for the given 1-based step, or empty if the car has no command for it. */
    public Optional<Command> commandAt(int step) {
        List<Command> commands = spec.commands();
        return step >= 1 && step <= commands.size()
                ? Optional.of(commands.get(step - 1))
                : Optional.empty();
    }

    /**
     * Executes a command. A forward move that would leave the field is ignored
     * and the car stays in place.
     */
    public void execute(Command command, Field field) {
        requireActive();
        switch (command) {
            case L -> direction = direction.turnLeft();
            case R -> direction = direction.turnRight();
            case F -> {
                Position target = position.moved(direction);
                if (field.contains(target)) {
                    position = target;
                }
            }
        }
    }

    /** Puts the car back on a previous cell, used when a move is cancelled by a head-on swap. */
    public void revertTo(Position previous) {
        requireActive();
        this.position = Objects.requireNonNull(previous, "previous");
    }

    /** Marks the car as having executed all of its commands. */
    public void finish() {
        requireActive();
        status = Status.FINISHED;
    }

    /** Marks the car as crashed. Both active and finished (parked) cars can be hit. */
    public void crash(List<String> otherCars, int step) {
        if (status == Status.COLLIDED) {
            throw new IllegalStateException("Car " + name() + " has already collided.");
        }
        status = Status.COLLIDED;
        collision = new Collision(otherCars, position, step);
    }

    private void requireActive() {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("Car " + name() + " is not active (" + status + ").");
        }
    }
}
