package com.carcrash.simulation;

import com.carcrash.domain.Direction;
import com.carcrash.domain.Position;

import java.util.List;

/** Final state of one car after a simulation run. */
public sealed interface CarOutcome {

    String name();

    /** The car did not collide; it ended at the given position facing the given direction. */
    record Finished(String name, Position position, Direction direction) implements CarOutcome {
    }

    /** The car collided with {@code otherCars} at {@code position} during {@code step}. */
    record Collided(String name, List<String> otherCars, Position position, int step) implements CarOutcome {
        public Collided {
            otherCars = List.copyOf(otherCars);
        }
    }
}
