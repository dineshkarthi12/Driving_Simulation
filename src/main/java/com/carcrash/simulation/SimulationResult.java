package com.carcrash.simulation;

import java.util.List;

/** Outcomes of a simulation run, one per car, in registration order. */
public record SimulationResult(List<CarOutcome> outcomes) {

    public SimulationResult {
        outcomes = List.copyOf(outcomes);
    }
}
