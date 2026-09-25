package com.carcrash.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldTest {

    private final Field field = new Field(10, 10);

    @ParameterizedTest
    @CsvSource({"0, 0", "9, 9", "0, 9", "9, 0", "5, 4"})
    void containsCellsInsideTheBoundary(int x, int y) {
        assertThat(field.contains(new Position(x, y))).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1", "10, 0", "0, 10", "10, 10"})
    void excludesCellsOutsideTheBoundary(int x, int y) {
        assertThat(field.contains(new Position(x, y))).isFalse();
    }

    @Test
    void nonSquareFieldUsesWidthForXAndHeightForY() {
        Field wide = new Field(5, 2);
        assertThat(wide.contains(new Position(4, 1))).isTrue();
        assertThat(wide.contains(new Position(1, 4))).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, 10", "10, 0", "-1, 5", "5, -3"})
    void rejectsNonPositiveDimensions(int width, int height) {
        assertThatThrownBy(() -> new Field(width, height)).isInstanceOf(IllegalArgumentException.class);
    }
}
