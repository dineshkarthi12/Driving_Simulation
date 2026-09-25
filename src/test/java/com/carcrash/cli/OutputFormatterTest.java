package com.carcrash.cli;

import com.carcrash.domain.CarSpec;
import com.carcrash.domain.Command;
import com.carcrash.domain.Direction;
import com.carcrash.domain.Position;
import com.carcrash.simulation.CarOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutputFormatterTest {

    @Test
    void formatsCarWithCommands() {
        CarSpec car = new CarSpec("A", new Position(1, 2), Direction.N, Command.parseAll("FFRFFFFRRL"));
        assertThat(OutputFormatter.formatCar(car)).isEqualTo("- A, (1,2) N, FFRFFFFRRL");
    }

    @Test
    void formatsParkedCar() {
        CarSpec car = new CarSpec("P", new Position(3, 3), Direction.E, List.of());
        assertThat(OutputFormatter.formatCar(car)).isEqualTo("- P, (3,3) E, (no commands)");
    }

    @Test
    void formatsFinishedOutcome() {
        CarOutcome outcome = new CarOutcome.Finished("A", new Position(5, 4), Direction.S);
        assertThat(OutputFormatter.formatOutcome(outcome)).isEqualTo("- A, (5,4) S");
    }

    @Test
    void formatsCollisionOutcome() {
        CarOutcome outcome = new CarOutcome.Collided("A", List.of("B"), new Position(5, 4), 7);
        assertThat(OutputFormatter.formatOutcome(outcome)).isEqualTo("- A, collides with B at (5,4) at step 7");
    }

    @Test
    void formatsCollisionWithSeveralCars() {
        CarOutcome outcome = new CarOutcome.Collided("A", List.of("B", "C"), new Position(1, 1), 2);
        assertThat(OutputFormatter.formatOutcome(outcome))
                .isEqualTo("- A, collides with B, C at (1,1) at step 2");
    }
}
