package com.carcrash.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.carcrash.domain.Command.F;
import static com.carcrash.domain.Command.L;
import static com.carcrash.domain.Command.R;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarTest {

    private final Field field = new Field(10, 10);

    private static Car car(int x, int y, Direction direction, Command... commands) {
        return new Car(new CarSpec("A", new Position(x, y), direction, List.of(commands)));
    }

    @Test
    void rotatesWithoutMoving() {
        Car car = car(1, 1, Direction.N);
        car.execute(L, field);
        assertThat(car.direction()).isEqualTo(Direction.W);
        car.execute(R, field);
        car.execute(R, field);
        assertThat(car.direction()).isEqualTo(Direction.E);
        assertThat(car.position()).isEqualTo(new Position(1, 1));
    }

    @Test
    void movesForwardInFacingDirection() {
        Car car = car(1, 1, Direction.E);
        car.execute(F, field);
        assertThat(car.position()).isEqualTo(new Position(2, 1));
    }

    @Test
    void ignoresForwardMoveBeyondBottomLeftBoundary() {
        Car south = car(0, 0, Direction.S);
        south.execute(F, field);
        assertThat(south.position()).isEqualTo(new Position(0, 0));

        Car west = car(0, 0, Direction.W);
        west.execute(F, field);
        assertThat(west.position()).isEqualTo(new Position(0, 0));
    }

    @Test
    void ignoresForwardMoveBeyondTopRightBoundary() {
        Car north = car(9, 9, Direction.N);
        north.execute(F, field);
        assertThat(north.position()).isEqualTo(new Position(9, 9));

        Car east = car(9, 9, Direction.E);
        east.execute(F, field);
        assertThat(east.position()).isEqualTo(new Position(9, 9));
    }

    @Test
    void commandsAreIndexedFromStepOne() {
        Car car = car(0, 0, Direction.N, F, L);
        assertThat(car.commandAt(1)).contains(F);
        assertThat(car.commandAt(2)).contains(L);
        assertThat(car.commandAt(3)).isEmpty();
        assertThat(car.commandAt(0)).isEmpty();
    }

    @Test
    void collidedCarRejectsFurtherCommands() {
        Car car = car(0, 0, Direction.N, F);
        car.crash(List.of("B"), 1);
        assertThat(car.hasCollided()).isTrue();
        assertThatThrownBy(() -> car.execute(F, field)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void finishedCarCanStillBeHit() {
        Car car = car(0, 0, Direction.N);
        car.finish();
        car.crash(List.of("B"), 3);
        assertThat(car.collision()).hasValueSatisfying(c -> {
            assertThat(c.otherCars()).containsExactly("B");
            assertThat(c.position()).isEqualTo(new Position(0, 0));
            assertThat(c.step()).isEqualTo(3);
        });
    }
}
