package com.carcrash.simulation;

import com.carcrash.domain.Command;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;
import com.carcrash.domain.Simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Runs a simulation step by step. All cars move simultaneously: during step N
 * every active car executes its Nth command, and collisions are evaluated once
 * all cars have acted.
 *
 * <p>Collision rules applied in each step:
 * <ol>
 *   <li><b>Head-on swap</b>: two cars that would exchange cells would pass
 *       through each other. Neither move happens, the cars stay on their
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
        return new SimulationResult(cars.stream().map(Car::outcome).toList());
    }

    private static void executeStep(Field field, List<Car> cars, int step) {
        Map<Car, Command> commands = new LinkedHashMap<>();
        for (Car car : cars) {
            if (car.isActive()) {
                car.commandAt(step).ifPresentOrElse(command -> commands.put(car, command), car::finish);
            }
        }

        Map<Car, TreeSet<String>> hits = new LinkedHashMap<>();
        Set<Car> swapped = detectSwaps(field, commands, hits);
        commands.forEach((car, command) -> {
            if (!swapped.contains(car)) {
                car.execute(command, field);
            }
        });
        detectSharedCells(cars, hits);

        hits.forEach((car, others) -> car.crash(List.copyOf(others), step));
    }

    /**
     * Finds pairs of cars that would exchange cells, records the collision and
     * returns those cars so that their moves are not carried out.
     */
    private static Set<Car> detectSwaps(Field field, Map<Car, Command> commands, Map<Car, TreeSet<String>> hits) {
        Map<Car, Position> targets = new HashMap<>();
        Map<Position, Car> movingFrom = new HashMap<>();
        commands.forEach((car, command) -> {
            Position target = car.plannedPosition(command, field);
            if (!target.equals(car.position())) {
                targets.put(car, target);
                movingFrom.put(car.position(), car);
            }
        });

        Set<Car> swapped = new HashSet<>();
        targets.forEach((car, target) -> {
            Car other = movingFrom.get(target);
            if (other != null && car.position().equals(targets.get(other))) {
                recordHit(hits, car, other.name());
                swapped.add(car);
            }
        });
        return swapped;
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
}
