package com.carcrash.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulationTest {

    private final Simulation simulation = new Simulation(new Field(10, 10));

    private static CarSpec spec(String name, int x, int y) {
        return new CarSpec(name, new Position(x, y), Direction.N, List.of(Command.F));
    }

    @Test
    void keepsCarsInRegistrationOrder() {
        simulation.addCar(spec("B", 1, 1));
        simulation.addCar(spec("A", 2, 2));
        assertThat(simulation.cars()).extracting(CarSpec::name).containsExactly("B", "A");
    }

    @Test
    void rejectsDuplicateName() {
        simulation.addCar(spec("A", 1, 1));
        assertThatThrownBy(() -> simulation.addCar(spec("A", 2, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        assertThat(simulation.cars()).hasSize(1);
    }

    @Test
    void namesAreCaseSensitive() {
        simulation.addCar(spec("A", 1, 1));
        simulation.addCar(spec("a", 2, 2));
        assertThat(simulation.cars()).hasSize(2);
    }

    @Test
    void rejectsOccupiedStartPosition() {
        simulation.addCar(spec("A", 1, 1));
        assertThatThrownBy(() -> simulation.addCar(spec("B", 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already occupied by car A");
    }

    @Test
    void rejectsStartPositionOutsideField() {
        assertThatThrownBy(() -> simulation.addCar(spec("A", 10, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outside the field");
        assertThatThrownBy(() -> simulation.addCar(spec("A", -1, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotBeModifiedThroughCarList() {
        simulation.addCar(spec("A", 1, 1));
        assertThatThrownBy(() -> simulation.cars().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
