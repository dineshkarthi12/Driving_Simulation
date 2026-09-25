package com.carcrash.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    @ParameterizedTest
    @CsvSource({"N, 3, 5", "E, 4, 4", "S, 3, 3", "W, 2, 4"})
    void movesOneStepInDirection(Direction direction, int expectedX, int expectedY) {
        assertThat(new Position(3, 4).moved(direction)).isEqualTo(new Position(expectedX, expectedY));
    }

    @Test
    void isImmutable() {
        Position original = new Position(1, 1);
        original.moved(Direction.N);
        assertThat(original).isEqualTo(new Position(1, 1));
    }

    @Test
    void hasValueEquality() {
        assertThat(new Position(2, 3)).isEqualTo(new Position(2, 3)).hasSameHashCodeAs(new Position(2, 3));
    }

    @Test
    void formatsLikeTheSpecification() {
        assertThat(new Position(5, 4)).hasToString("(5,4)");
    }
}
