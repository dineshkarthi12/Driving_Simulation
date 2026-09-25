package com.carcrash.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DirectionTest {

    @ParameterizedTest
    @CsvSource({"N, W", "W, S", "S, E", "E, N"})
    void turnLeftRotatesCounterClockwise(Direction from, Direction expected) {
        assertThat(from.turnLeft()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"N, E", "E, S", "S, W", "W, N"})
    void turnRightRotatesClockwise(Direction from, Direction expected) {
        assertThat(from.turnRight()).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void fourTurnsInEitherDirectionReturnToStart(Direction direction) {
        assertThat(direction.turnLeft().turnLeft().turnLeft().turnLeft()).isEqualTo(direction);
        assertThat(direction.turnRight().turnRight().turnRight().turnRight()).isEqualTo(direction);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void leftAndRightCancelOut(Direction direction) {
        assertThat(direction.turnLeft().turnRight()).isEqualTo(direction);
    }

    @Test
    void movementDeltasPointAlongTheAxes() {
        assertThat(new int[] {Direction.N.dx(), Direction.N.dy()}).containsExactly(0, 1);
        assertThat(new int[] {Direction.E.dx(), Direction.E.dy()}).containsExactly(1, 0);
        assertThat(new int[] {Direction.S.dx(), Direction.S.dy()}).containsExactly(0, -1);
        assertThat(new int[] {Direction.W.dx(), Direction.W.dy()}).containsExactly(-1, 0);
    }

    @ParameterizedTest
    @CsvSource({"N, N", "n, N", "' e ', E", "s, S", "W, W"})
    void parsesSymbolsIgnoringCaseAndWhitespace(String symbol, Direction expected) {
        assertThat(Direction.fromSymbol(symbol)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "X", "NE", "North", "1"})
    void rejectsUnknownSymbols(String symbol) {
        assertThatThrownBy(() -> Direction.fromSymbol(symbol))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid direction");
    }

    @Test
    void rejectsNullSymbol() {
        assertThatThrownBy(() -> Direction.fromSymbol(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
