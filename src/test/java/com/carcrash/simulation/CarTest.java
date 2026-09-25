package com.carcrash.simulation;

import com.carcrash.domain.Direction;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.carcrash.domain.Command.F;
import static com.carcrash.domain.Command.L;
import static com.carcrash.domain.Command.R;
import static com.carcrash.domain.TestCars.car;
import static org.assertj.core.api.Assertions.assertThat;

class CarTest {

    private final Field field = new Field(10, 10);

    private static Car newCar(int x, int y, String direction, String commands) {
        return new Car(car("A", x, y, direction, commands));
    }

    @Test
    void rotatesWithoutMoving() {
        Car car = newCar(1, 1, "N", "");
        car.execute(L, field);
        car.execute(R, field);
        car.execute(R, field);
        assertThat(car.outcome()).isEqualTo(new CarOutcome.Finished("A", new Position(1, 1), Direction.E));
    }

    @Test
    void movesForwardInFacingDirection() {
        Car car = newCar(1, 1, "E", "");
        car.execute(F, field);
        assertThat(car.position()).isEqualTo(new Position(2, 1));
    }

    @Test
    void ignoresForwardMoveBeyondBottomLeftBoundary() {
        Car south = newCar(0, 0, "S", "");
        south.execute(F, field);
        assertThat(south.position()).isEqualTo(new Position(0, 0));

        Car west = newCar(0, 0, "W", "");
        west.execute(F, field);
        assertThat(west.position()).isEqualTo(new Position(0, 0));
    }

    @Test
    void ignoresForwardMoveBeyondTopRightBoundary() {
        Car north = newCar(9, 9, "N", "");
        north.execute(F, field);
        assertThat(north.position()).isEqualTo(new Position(9, 9));

        Car east = newCar(9, 9, "E", "");
        east.execute(F, field);
        assertThat(east.position()).isEqualTo(new Position(9, 9));
    }

    @Test
    void plannedPositionDoesNotChangeTheCar() {
        Car car = newCar(1, 1, "N", "");
        assertThat(car.plannedPosition(F, field)).isEqualTo(new Position(1, 2));
        assertThat(car.plannedPosition(L, field)).isEqualTo(new Position(1, 1));
        assertThat(car.position()).isEqualTo(new Position(1, 1));
    }

    @Test
    void commandsAreIndexedFromStepOne() {
        Car car = newCar(0, 0, "N", "FL");
        assertThat(car.commandAt(1)).contains(F);
        assertThat(car.commandAt(2)).contains(L);
        assertThat(car.commandAt(3)).isEmpty();
        assertThat(car.commandAt(0)).isEmpty();
    }

    @Test
    void finishedCarIsInactiveButCanStillBeHit() {
        Car car = newCar(0, 0, "N", "");
        car.finish();
        assertThat(car.isActive()).isFalse();

        car.crash(List.of("B"), 3);
        assertThat(car.hasCollided()).isTrue();
        assertThat(car.outcome()).isEqualTo(new CarOutcome.Collided("A", List.of("B"), new Position(0, 0), 3));
    }
}
