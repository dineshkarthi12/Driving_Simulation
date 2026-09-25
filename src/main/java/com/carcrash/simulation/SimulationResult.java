package com.carcrash.simulation;

import java.util.List;
import java.util.Optional;

/** Outcomes of a simulation run, one per car, in registration order. */
public record SimulationResult(List<CarOutcome> outcomes) {

    public SimulationResult {
        outcomes = List.copyOf(outcomes);
    }

    /** Looks up the outcome of a car by name. */
    public Optional<CarOutcome> outcomeOf(String name) {
        return outcomes.stream().filter(outcome -> outcome.name().equals(name)).findFirst();
    }
}
