package com.carcrash.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A field together with the cars registered on it, in registration order.
 * Enforces the registration rules: car names are unique and every car starts
 * on its own cell inside the field.
 */
public final class Simulation {

    private final Field field;
    private final List<CarSpec> cars = new ArrayList<>();

    public Simulation(Field field) {
        this.field = Objects.requireNonNull(field, "field");
    }

    public Field field() {
        return field;
    }

    /** Registered cars in the order they were added. */
    public List<CarSpec> cars() {
        return List.copyOf(cars);
    }

    public boolean hasCars() {
        return !cars.isEmpty();
    }

    /** Whether every cell of the field is taken, so no further car can be placed. */
    public boolean isFull() {
        return cars.size() >= (long) field.width() * field.height();
    }

    /**
     * Validates a proposed car name.
     *
     * @throws IllegalArgumentException if the name is blank or already used
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Car name must not be empty.");
        }
        if (cars.stream().anyMatch(car -> car.name().equals(name))) {
            throw new IllegalArgumentException("A car named " + name + " already exists.");
        }
    }

    /**
     * Validates a proposed starting position.
     *
     * @throws IllegalArgumentException if the position is outside the field or already occupied
     */
    public void validateStartPosition(Position position) {
        if (!field.contains(position)) {
            throw new IllegalArgumentException("Position " + position + " is outside the field. Valid x is 0.."
                    + (field.width() - 1) + " and valid y is 0.." + (field.height() - 1) + ".");
        }
        cars.stream()
                .filter(car -> car.start().equals(position))
                .findFirst()
                .ifPresent(car -> {
                    throw new IllegalArgumentException(
                            "Position " + position + " is already occupied by car " + car.name() + ".");
                });
    }

    /**
     * Registers a car after validating its name and starting position.
     *
     * @throws IllegalArgumentException if any registration rule is violated
     */
    public void addCar(CarSpec car) {
        Objects.requireNonNull(car, "car");
        validateName(car.name());
        validateStartPosition(car.start());
        cars.add(car);
    }
}
