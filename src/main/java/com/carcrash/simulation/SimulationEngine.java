package com.carcrash.simulation;

import com.carcrash.domain.Car;
import com.carcrash.domain.Command;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;
import com.carcrash.domain.Simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Runs a simulation step by step. All cars move simultaneously: during step N
 * every active car executes its Nth command, and collisions are evaluated once
 * all cars have acted.
 *
 * <p>Collision rules applied after each step:
 * <ol>
 *   <li><b>Head-on swap</b>: two cars that exchanged cells would have passed
 *       through each other. Both moves are cancelled, the cars stay on their
 *       pre-move cells, and each collides with the other at its own cell.</li>
 *   <li><b>Shared cell</b>: two or more cars on the same cell collide. Every car
 *       there that has not already collided stops, including finished or parked
 *       cars that are hit. Cars that collided in an earlier step keep their
 *       original collision report.</li>
 * </ol>
 * A car taking part in both kinds of collision in the same step reports every
 * car it collided with. Other cars are listed in alphabetical order.
 *
 * <p>The engine is stateless and does not modify the given {@link Simulation}.
 */
public final class SimulationEngine {

    public SimulationResult run(Simulation simulation) {
        Field field = simulation.field();
        List<Car> cars = simulation.cars().stream().map(Car::new).toList();

        int step = 0;
        while (cars.stream().anyMatch(Car::isActive)) {
            step++;
            executeStep(field, cars, step);
        }
        return new SimulationResult(cars.stream().map(SimulationEngine::toOutcome).toList());
    }

    private static void executeStep(Field field, List<Car> cars, int step) {
        Map<Car, Position> movedFrom = new LinkedHashMap<>();
        for (Car car : cars) {
            if (!car.isActive()) {
                continue;
            }
            Optional<Command> command = car.commandAt(step);
            if (command.isEmpty()) {
                car.finish();
                continue;
            }
            Position before = car.position();
            car.execute(command.get(), field);
            if (!car.position().equals(before)) {
                movedFrom.put(car, before);
            }
        }

        Map<Car, TreeSet<String>> hits = new LinkedHashMap<>();
        detectSwaps(movedFrom, hits);
        detectSharedCells(cars, hits);

        hits.forEach((car, others) -> car.crash(List.copyOf(others), step));
    }

    /** Detects pairs of cars that swapped cells and cancels both moves. */
    private static void detectSwaps(Map<Car, Position> movedFrom, Map<Car, TreeSet<String>> hits) {
        Map<Position, Car> carLeaving = new HashMap<>();
        movedFrom.forEach((car, from) -> carLeaving.put(from, car));

        List<Car> swapped = new ArrayList<>();
        movedFrom.forEach((car, from) -> {
            Car other = carLeaving.get(car.position());
            if (other != null && other != car && from.equals(other.position())) {
                recordHit(hits, car, other.name());
                swapped.add(car);
            }
        });
        swapped.forEach(car -> car.revertTo(movedFrom.get(car)));
    }

    /** Detects cells occupied by more than one car after the step's moves. */
    private static void detectSharedCells(List<Car> cars, Map<Car, TreeSet<String>> hits) {
        Map<Position, List<Car>> byCell = new LinkedHashMap<>();
        cars.forEach(car -> byCell.computeIfAbsent(car.position(), p -> new ArrayList<>()).add(car));

        for (List<Car> occupants : byCell.values()) {
            if (occupants.size() < 2) {
                continue;
            }
            for (Car car : occupants) {
                if (car.hasCollided()) {
                    continue;
                }
                occupants.stream()
                        .filter(other -> other != car)
                        .forEach(other -> recordHit(hits, car, other.name()));
            }
        }
    }

    private static void recordHit(Map<Car, TreeSet<String>> hits, Car car, String otherName) {
        hits.computeIfAbsent(car, c -> new TreeSet<>()).add(otherName);
    }

    private static CarOutcome toOutcome(Car car) {
        return car.collision()
                .<CarOutcome>map(c -> new CarOutcome.Collided(car.name(), c.otherCars(), c.position(), c.step()))
                .orElseGet(() -> new CarOutcome.Finished(car.name(), car.position(), car.direction()));
    }
}
