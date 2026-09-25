package com.carcrash.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulationTest {

    private final Simulation simulation = new Simulation(new Field(10, 10));

    private static CarSpec spec(String name, int x, int y) {
        return TestCars.car(name, x, y, "N", "F");
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
    void rejectsBlankName() {
        assertThatThrownBy(() -> simulation.validateName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Car name must not be empty.");
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

    @Test
    void isFullOnceEveryCellIsTaken() {
        Simulation tiny = new Simulation(new Field(2, 1));
        assertThat(tiny.isFull()).isFalse();
        tiny.addCar(spec("A", 0, 0));
        assertThat(tiny.isFull()).isFalse();
        tiny.addCar(spec("B", 1, 0));
        assertThat(tiny.isFull()).isTrue();
    }

    @Test
    void isFullDoesNotOverflowOnHugeFields() {
        assertThat(new Simulation(new Field(Integer.MAX_VALUE, Integer.MAX_VALUE)).isFull()).isFalse();
    }
}
